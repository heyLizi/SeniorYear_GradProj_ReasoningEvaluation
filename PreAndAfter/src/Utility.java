
public class Utility {

    /**将汉字表示的序号转为数字表达
     *
     * @param characterNum 汉字表示的序号
     * @return 转换后的数字序号
     */
    public static int characterToInt(String characterNum) {
        String intStr = "";

        for(int i=0; i<characterNum.length(); i++) {
            char tempChar = characterNum.charAt(i);
            switch (tempChar) {
                case '一': intStr += "1"; break;
                case '二': intStr += "2"; break;
                case '三': intStr += "3"; break;
                case '四': intStr += "4"; break;
                case '五': intStr += "5"; break;
                case '六': intStr += "6"; break;
                case '七': intStr += "7"; break;
                case '八': intStr += "8"; break;
                case '九': intStr += "9"; break;
                case '零': intStr += "0"; break;
                default: break;
            }
        }

        return Integer.parseInt(intStr);
    }


    /**将数字表示的序号转为汉字表达
     *
     * @param intNum 数字表示的序号
     * @return
     */
    public static String intToCharacter(int intNum) {
        String intStr = intNum+"";
        int strLength = intStr.length();
        char[] charArr = {' ', '十', '百', '千', '万'};

        String characterStr = "";
        for(int i=0; i<strLength; i++) {
            char tempChar = intStr.charAt(i);
            switch (tempChar) {
                case '1': characterStr += "一"; break;
                case '2': characterStr += "二"; break;
                case '3': characterStr += "三"; break;
                case '4': characterStr += "四"; break;
                case '5': characterStr += "五"; break;
                case '6': characterStr += "六"; break;
                case '7': characterStr += "七"; break;
                case '8': characterStr += "八"; break;
                case '9': characterStr += "九"; break;
                case '0': characterStr += "零"; break;
                default: break;
            }
            characterStr += charArr[strLength - i - 1];
        }

        return characterStr.trim();
    }

}
