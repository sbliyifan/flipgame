package cn.edu.sicnu.flipgame

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.flipgame.model.CardMatchingGame
import cn.edu.sicnu.flipgame.view.CardAdapter

/**
 * 游戏界面（Controller 层）。
 *
 * 职责：把 Model（CardMatchingGame）和 View（RecyclerView + CardAdapter）连起来：
 *   - 收到 Adapter 抛上来的点击回调 -> 交给 Model 处理
 *   - 根据 Model 返回的结果刷新界面、延迟回翻、弹通关对话框
 *
 * 横竖屏用不同的列数：竖屏 4 列（6 行），横屏 6 列（4 行），
 * 牌的总数都是 24 张，但一屏的排布不同，这就是"横屏竖屏不同牌数"的实现方式。
 */
class GameActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvScore: TextView
    private lateinit var tvFlip: TextView
    private lateinit var adapter: CardAdapter
    private lateinit var game: CardMatchingGame

    private val handler = Handler(Looper.getMainLooper())

    /** 匹配失败、等待回翻的这段时间里，禁止继续点牌 */
    private var isLocked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvScore = findViewById(R.id.tv_score)
        tvFlip = findViewById(R.id.tv_flip)
        recyclerView = findViewById(R.id.rv_cards)

        val symbols = resources.getStringArray(R.array.card_symbols).toList()
        val cardsPerGroup = intent.getIntExtra(EXTRA_CARDS_PER_GROUP, CardMatchingGame.DEFAULT_CARDS_PER_GROUP)

        game = CardMatchingGame(CARD_COUNT, symbols, cardsPerGroup)
        restoreIfNeeded(savedInstanceState)

        // 横竖屏用不同列数
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val columns = if (isLandscape) COLUMNS_LANDSCAPE else COLUMNS_PORTRAIT
        recyclerView.layoutManager = GridLayoutManager(this, columns)

        adapter = CardAdapter(game.cards) { index -> onCardChosen(index) }
        recyclerView.adapter = adapter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        updateStatusBar()
    }

    /** 点击某一张牌 */
    private fun onCardChosen(index: Int) {
        if (isLocked || game.isGameOver) return

        val result = game.chooseCardAtIndex(index)
        adapter.notifyDataSetChanged()
        updateStatusBar()

        if (result.pendingFlipBack.isNotEmpty()) {
            // 没配上：先锁住，过一会儿自动翻回去
            isLocked = true
            handler.postDelayed({
                game.flipDown(result.pendingFlipBack)
                adapter.notifyDataSetChanged()
                updateStatusBar()
                isLocked = false
            }, FLIP_BACK_DELAY_MS)
        }

        if (result.gameOver) {
            handler.postDelayed({ showGameOverDialog() }, FLIP_BACK_DELAY_MS)
        }
    }

    private fun updateStatusBar() {
        tvScore.text = getString(R.string.score_format, game.score)
        tvFlip.text = getString(R.string.flip_format, game.flipCount)
    }

    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.game_over_title)
            .setMessage(getString(R.string.game_over_message, game.score, game.flipCount))
            .setPositiveButton(R.string.action_restart) { _, _ -> restart() }
            .setNegativeButton(R.string.ok, null)
            .show()
    }

    /** 重新开一局（Model 的 reset 方法） */
    private fun restart() {
        handler.removeCallbacksAndMessages(null)
        isLocked = false
        game.reset()
        adapter.notifyDataSetChanged()
        updateStatusBar()
    }

    // ---------------- 状态保存与恢复（横竖屏切换用） ----------------

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val states = IntArray(game.cards.size)
        game.cards.forEachIndexed { i, card ->
            states[i] = when {
                card.isMatched -> STATE_MATCHED
                card.isFaceUp -> STATE_FACE_UP
                else -> STATE_FACE_DOWN
            }
        }
        outState.putIntArray(KEY_STATES, states)
        outState.putString(KEY_ORDER, game.cards.joinToString(SEPARATOR) { it.symbol })
        outState.putInt(KEY_SCORE, game.score)
        outState.putInt(KEY_FLIP, game.flipCount)
    }

    private fun restoreIfNeeded(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        val states = savedInstanceState.getIntArray(KEY_STATES) ?: return
        val order = savedInstanceState.getString(KEY_ORDER)?.split(SEPARATOR) ?: return
        if (order.size != states.size) return
        game.restore(
            order,
            states,
            savedInstanceState.getInt(KEY_SCORE, 0),
            savedInstanceState.getInt(KEY_FLIP, 0)
        )
    }

    // ---------------- 菜单 ----------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_game, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_restart -> {
                restart()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        /** 牌的总数：24 张 */
        const val CARD_COUNT = 24

        /** 竖屏 4 列 × 6 行 */
        private const val COLUMNS_PORTRAIT = 4

        /** 横屏 6 列 × 4 行 */
        private const val COLUMNS_LANDSCAPE = 6

        /** 匹配失败后停留多久再翻回去 */
        private const val FLIP_BACK_DELAY_MS = 700L

        const val EXTRA_CARDS_PER_GROUP = "extra_cards_per_group"

        private const val KEY_STATES = "key_states"
        private const val KEY_ORDER = "key_order"
        private const val KEY_SCORE = "key_score"
        private const val KEY_FLIP = "key_flip"
        private const val SEPARATOR = ","

        private const val STATE_FACE_DOWN = 0
        private const val STATE_FACE_UP = 1
        private const val STATE_MATCHED = 2
    }
}
