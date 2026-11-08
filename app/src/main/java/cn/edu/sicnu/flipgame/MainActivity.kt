package cn.edu.sicnu.flipgame

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * 主界面（Controller 层）。
 *
 * 本页承担两项作业要求：
 *   1) 提供进入游戏的入口，可以选 2 张玩法或 3 张玩法；
 *   2) 在 ActionBar 上提供菜单，点击"关于"弹出关于对话框。
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_mode_two).setOnClickListener {
            startGame(cardsPerGroup = 2)
        }

        findViewById<Button>(R.id.btn_mode_three).setOnClickListener {
            startGame(cardsPerGroup = 3)
        }
    }

    private fun startGame(cardsPerGroup: Int) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_CARDS_PER_GROUP, cardsPerGroup)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAboutDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 关于对话框：说明作者、玩法与 MVC 结构 */
    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(R.string.about_message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}
