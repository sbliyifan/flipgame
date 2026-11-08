package cn.edu.sicnu.flipgame.model

/**
 * 游戏模型（Model 层）：负责发牌、计分、判断匹配等全部规则，
 * 不持有任何 View 引用，所以可以单独测试，也方便在 2 张 / 3 张玩法之间切换。
 *
 * @param cardCount      牌的总数（本实验为 24）
 * @param symbols        可用的花色（来自 strings.xml 的 card_symbols）
 * @param cardsPerGroup  几张牌算一组：2 表示 2 张玩法，3 表示 3 张玩法
 */
class CardMatchingGame(
    private val cardCount: Int,
    private val symbols: List<String>,
    private val cardsPerGroup: Int = DEFAULT_CARDS_PER_GROUP
) {

    /** 牌堆，顺序就是界面上显示的先后顺序 */
    val cards: MutableList<Card> = mutableListOf()

    /** 当前已经翻开、但还没凑够一组的牌 */
    private val selected: MutableList<Card> = mutableListOf()

    var score: Int = 0
        private set

    var flipCount: Int = 0
        private set

    init {
        reset()
    }

    /**
     * 重置游戏：重新发牌、清空分数与翻牌次数。
     * 这是作业要求给 CardMatchingGame 新增的方法。
     */
    fun reset() {
        cards.clear()
        selected.clear()
        score = 0
        flipCount = 0

        val groupCount = cardCount / cardsPerGroup
        val pool = mutableListOf<Card>()
        var id = 0
        for (g in 0 until groupCount) {
            val symbol = symbols[g % symbols.size]
            repeat(cardsPerGroup) {
                pool.add(Card(id++, symbol))
            }
        }
        pool.shuffle()          // 洗牌，保证每局位置都不一样
        cards.addAll(pool)
    }

    /** 是否已经全部配对完成 */
    val isGameOver: Boolean
        get() = cards.isNotEmpty() && cards.all { it.isMatched }

    /**
     * 点击某一张牌。
     *
     * 返回 [ChooseResult]，由 Controller 决定界面怎么刷新、要不要延迟翻回，
     * 这样 Model 依旧不接触界面（MVC 的分工）。
     */
    fun chooseCardAtIndex(index: Int): ChooseResult {
        val card = cards[index]

        // 已经配对的牌不能再点
        if (card.isMatched) {
            return ChooseResult(card, emptyList(), emptyList(), isGameOver)
        }

        // 再点一次已翻开的牌 —— 收回去
        if (card.isFaceUp) {
            card.isFaceUp = false
            selected.remove(card)
            return ChooseResult(card, emptyList(), emptyList(), isGameOver)
        }

        // 翻开一张新牌
        card.isFaceUp = true
        flipCount++
        selected.add(card)

        // 还没凑够一组，先记着，扣 1 分
        if (selected.size < cardsPerGroup) {
            score -= COST_PER_FLIP
            return ChooseResult(card, emptyList(), emptyList(), isGameOver)
        }

        // 凑够一组，开始判断
        val group = selected.toList()
        val first = group.first()
        val others = group.drop(1)

        return if (first.match(others)) {
            // 匹配成功：全部标记为已配对，加分
            group.forEach { it.isMatched = true }
            score += MATCH_BONUS * cardsPerGroup
            selected.clear()
            ChooseResult(card, group, emptyList(), isGameOver)
        } else {
            // 匹配失败：扣分，并把这一组交给 Controller 延迟翻回去
            score -= MISMATCH_PENALTY
            selected.clear()
            ChooseResult(card, emptyList(), group, isGameOver)
        }
    }

    /** 把一组没有匹配成功的牌翻回去（由 Controller 延迟调用） */
    fun flipDown(cards: List<Card>) {
        cards.forEach { if (!it.isMatched) it.isFaceUp = false }
    }

    /** 恢复某一局的状态：横竖屏切换、Activity 重建后会用到 */
    fun restore(symbolOrder: List<String>, states: IntArray, savedScore: Int, savedFlip: Int) {
        cards.clear()
        selected.clear()
        symbolOrder.forEachIndexed { i, s ->
            val card = Card(i, s)
            when (states.getOrNull(i) ?: 0) {
                1 -> card.isFaceUp = true
                2 -> card.isMatched = true
            }
            cards.add(card)
            if (card.isFaceUp) selected.add(card)
        }
        score = savedScore
        flipCount = savedFlip
    }

    /**
     * 一次点击之后的结果。
     * @param card           被点击的牌
     * @param matchedGroup   本次凑成的一组（没凑成时为空）
     * @param pendingFlipBack 需要翻回去的牌（匹配失败时非空）
     * @param gameOver       是否已经通关
     */
    data class ChooseResult(
        val card: Card,
        val matchedGroup: List<Card>,
        val pendingFlipBack: List<Card>,
        val gameOver: Boolean
    )

    companion object {
        /** 默认 2 张牌一组 */
        const val DEFAULT_CARDS_PER_GROUP = 2

        /** 每翻一张牌扣的分数 */
        private const val COST_PER_FLIP = 1

        /** 匹配成功的基础加分（会乘以组大小） */
        private const val MATCH_BONUS = 10

        /** 匹配失败扣的分数 */
        private const val MISMATCH_PENALTY = 5
    }
}
