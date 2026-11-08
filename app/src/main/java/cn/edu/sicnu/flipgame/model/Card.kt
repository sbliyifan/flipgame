package cn.edu.sicnu.flipgame.model

/**
 * 一张牌（Model 层的实体类）。
 *
 * symbol 表示牌面花色。花色相同的牌组成一组，可以互相匹配：
 *   2 张玩法：每种花色 2 张，凑够 2 张同花色即匹配；
 *   3 张玩法：每种花色 3 张，凑够 3 张同花色才算匹配。
 *
 * 这个类只保存"牌的状态"和"匹配规则"，完全不涉及界面，
 * 界面长什么样由 Adapter（View 层）决定。
 */
class Card(
    /** 在牌堆中的位置，Adapter 靠它定位被点击的是哪一张 */
    val index: Int,
    /** 牌面花色，取值来自 strings.xml 中的 card_symbols 资源数组 */
    val symbol: String
) {

    /** 是否正面朝上 */
    var isFaceUp: Boolean = false

    /** 是否已经配对成功（成功的牌不再参与点击） */
    var isMatched: Boolean = false

    /**
     * 判断自己与另外若干张牌能否凑成一组。
     *
     * 作业要求"修改游戏逻辑只需要修改 Card 的 match 方法"，
     * 所以把匹配规则集中到这里：只要 others 里每张牌的花色都与自己相同，
     * 就认为匹配成功。2 张玩法时 others 有 1 张，3 张玩法时 others 有 2 张，
     * 两种玩法共用同一段判断，不需要写两套逻辑。
     */
    fun match(others: List<Card>): Boolean {
        if (others.isEmpty()) return false
        return others.all { it.symbol == symbol }
    }

    /** 复位到初始状态（重新发牌时用） */
    fun reset() {
        isFaceUp = false
        isMatched = false
    }
}
