package cn.edu.sicnu.flipgame.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.flipgame.R
import cn.edu.sicnu.flipgame.model.Card
import com.google.android.material.card.MaterialCardView

/**
 * 牌堆适配器（View / Adapter 层）。
 *
 * 继承 RecyclerView.Adapter，重写三个必须实现的方法：
 *   onCreateViewHolder —— 创建 item 的 ViewHolder（把 item_card.xml 加载成 View）
 *   onBindViewHolder   —— 把第 position 张牌的数据“绑定”到 ViewHolder 上
 *   getItemCount       —— 告诉 RecyclerView 一共有多少条数据
 *
 * 同时通过构造参数把点击事件回调给 Controller（GameActivity），
 * 这样 Adapter 不需要知道游戏规则，只管“长什么样、被点了就往上抛”。
 */
class CardAdapter(
    private val cards: List<Card>,
    private val onCardClick: (Int) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    /** 每个 item 的持有者，缓存控件引用，避免每次绑定都 findViewById */
    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardRoot: MaterialCardView = itemView.findViewById(R.id.card_root)
        private val tvSymbol: TextView = itemView.findViewById(R.id.tv_symbol)
        private val tvBack: TextView = itemView.findViewById(R.id.tv_back)

        fun bind(card: Card) {
            val context = itemView.context
            val showFace = card.isFaceUp || card.isMatched

            // 正面显示花色，背面显示问号
            tvSymbol.text = card.symbol
            tvSymbol.visibility = if (showFace) View.VISIBLE else View.INVISIBLE
            tvBack.visibility = if (showFace) View.INVISIBLE else View.VISIBLE

            // 三种状态的背景色
            val bgColor = when {
                card.isMatched -> ContextCompat.getColor(context, R.color.card_matched)
                card.isFaceUp -> ContextCompat.getColor(context, R.color.card_face)
                else -> ContextCompat.getColor(context, R.color.card_back)
            }
            cardRoot.setCardBackgroundColor(bgColor)

            // 已配对的牌做成半透明，视觉上和还能点的牌区分开
            cardRoot.alpha = if (card.isMatched) 0.45f else 1f

            // 点击回调给 Controller，注意传的是这张牌在牌堆中的位置
            cardRoot.setOnClickListener { onCardClick(card.index) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position])
    }

    override fun getItemCount(): Int = cards.size
}
