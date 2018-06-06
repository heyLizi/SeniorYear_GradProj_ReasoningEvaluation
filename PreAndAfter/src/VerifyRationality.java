/*如何判定相关：
思路1：将事实句拆分得到关键词，前件含有事实句关键词之一即认为是相关
思路2：将前件句拆分得到关键词，事实句含有前件句关键词即认为是相关
思路3：分别比较事实句和前件句的短文本相似度，若大于一定阈值则认为是相关
*/

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyRationality {

    public static String[] crimePrincipalPunishmentType = {"管制", "拘役", "有期徒刑", "无期徒刑", "死刑"}; //刑法主刑类型
    public static String[] crimeSupplementaryPunishmentType = {"罚金", "剥夺政治权利", "没收财产"};    //刑法附加刑类型
    public static String[] punishmentExecutionType = {"缓刑", "减刑", "假释"}; //刑法刑罚执行制度

    /**验证事实与判决结果之间的合理性
     *
     * @param fact 事实
     * @param judgement 判决结果
     * @param referredArticles 引用的法条列表
     * @return 结果，若匹配，则MatchResult中的原因无意义；若不匹配，则MatchResult的原因表示不匹配的原因
     */
    public static MatchResultModel verifyRationality(String fact, String judgement, List<String> referredArticles) {
        MatchResultModel matchResultModel;

        String[] factWords = DealWithSentences.dealWithASentence(fact); //事实句拆分得到的有用数组
        if(factWords != null) {
            for(int i=0; i<factWords.length; i++) {
                System.out.print(factWords[i]+" ");
            }
        }
        System.out.println();

        List<PreAndAfterModel> preAndAfterModels = getArticleContent(referredArticles); //所引用的法条对应的全部前后件结果
        Set<Integer> matchedArticles = new HashSet<Integer>(); //保存被成功匹配的法条在列表中的下标，这里因为需要删除重复元素，所以使用Set类

        for(int i=0; i<factWords.length; i++) {
            String word = factWords[i];
            if(word != "" && !word.startsWith("null")) {
                String keyword = word.split(", ")[0];
                for(int j=0; j<preAndAfterModels.size(); j++) {
                    PreAndAfterModel paam = preAndAfterModels.get(j);
                    if(paam.getAntecedent().contains(keyword)) { //法条的前件中包含这个单词，即认为是匹配成功
                        matchedArticles.add(j);
                    }
                    else if(paam.getConsequent().contains(keyword)) { //法条的后件中包含这个单词，也认为是匹配成功
                        matchedArticles.add(j);
                    }
                }
            }
        }

        /*拆分前件得到有用数组
        for(int i=0; i<preAndAfterModels.size(); i++) {
            String[] keywords = DealWithSentences.dealWithASentence(preAndAfterModels.get(i).getAntecedent());
            if(keywords != null) {
                for(int j=0; j<keywords.length; j++) {
                    System.out.print(keywords[j]+", ");
                }
            }
            System.out.println();
        }*/

        if(matchedArticles.size() == 0) { //如果没有被成功匹配的法条
            matchResultModel = new MatchResultModel(false, "引用的法条中没有相关内容");
            return matchResultModel;
        }
        else {//如果有匹配的法条，查看匹配法条的后件

            String principalPunishmentStr = null; //主刑量刑部分
            String supplementaryPunishmentStr = null; //附加刑量刑部分
            ArrayList<String> principalPunishmentList = new ArrayList<String>(); //主刑量刑部分数组，判决结果可能有多个主刑的量刑程度，需要用列表保存
            ArrayList<String> supplementaryPunishmentList = new ArrayList<String>(); //附加刑量刑部分数组，判决结果可能有多个附加刑的量刑程度，需要用列表保存
            ArrayList<String> punishmentExecutionList = new ArrayList<String>(); //刑罚执行类型部分数组，判决结果可能有多个刑罚执行类型，需要用列表保存


            Iterator<Integer> it = matchedArticles.iterator();
            while(it.hasNext()) {
                PreAndAfterModel paam = preAndAfterModels.get(it.next());
                String antecedent = paam.getAntecedent();
                String consequent = paam.getConsequent();
                System.out.println(antecedent+", "+consequent);

                String punishmentStr = null;

                //可能有与关系、或关系，怎么判断？（“并处”表示与关系，连接主刑与附加刑；“、”“或者”表示或关系，连接同一个刑罚级别）
                if(consequent.contains("处")) {
                    punishmentStr = consequent.substring(consequent.indexOf("处") + 1, consequent.length()); //法条推荐量刑部分即为第一个“处”字之后的部分
                }
                else if(antecedent.contains("处")){
                    punishmentStr = antecedent.substring(consequent.indexOf("处") + 1, antecedent.length()); //法条推荐量刑部分即为第一个“处”字之后的部分
                }

                if(punishmentStr != null) {
                    //分离主刑与附加刑
                    if(punishmentStr.contains("并处")) {
                        principalPunishmentStr = punishmentStr.split("并处")[0];
                        supplementaryPunishmentStr = punishmentStr.split("并处")[1];
                    }
                    else{
                        principalPunishmentStr = punishmentStr;
                    }

                    //分别分离主刑与附加刑中的组成部分
                    if(principalPunishmentStr != null) {
                        if(principalPunishmentStr.contains("或者")) {
                            String[] temp = principalPunishmentStr.split("或者");
                            for(int i=0; i<temp.length; i++) {
                                principalPunishmentList.add(temp[i]);
                            }
                        }
                        else{
                            principalPunishmentList.add(principalPunishmentStr);
                        }
                        for(int i=0; i<principalPunishmentList.size(); i++) {
                            if(principalPunishmentList.get(i).contains("、")) {
                                String[] temp = principalPunishmentList.get(i).split("、");
                                for(int j=0; j<temp.length; j++) {
                                    principalPunishmentList.add(temp[j]);
                                }
                                principalPunishmentList.set(i, "");
                            }
                        }
                    }
                    if(supplementaryPunishmentStr != null) {
                        if(supplementaryPunishmentStr.contains("或者")) {
                            String[] temp = supplementaryPunishmentStr.split("或者");
                            for(int i=0; i<temp.length; i++) {
                                supplementaryPunishmentList.add(temp[i]);
                            }
                        }
                        else{
                            supplementaryPunishmentList.add(supplementaryPunishmentStr);
                        }
                        for(int i=0; i<supplementaryPunishmentList.size(); i++) {
                            if(supplementaryPunishmentList.get(i).contains("、")) {
                                String[] temp = supplementaryPunishmentList.get(i).split("、");
                                for(int j=0; j<temp.length; j++) {
                                    supplementaryPunishmentList.add(temp[j]);
                                }
                                supplementaryPunishmentList.set(i, "");
                            }
                        }
                    }
                }
                else { //如果不含有“处”，看是否含有刑罚制度
                    for(int i=0; i<punishmentExecutionType.length; i++) {
                        if(consequent.contains(punishmentExecutionType[i])) { //如果含有刑罚执行制度的
                            punishmentExecutionList.add(consequent);
                        }
                    }
                }
            }

            //分离判决结果的组成部分
            String[] judgementPunishments = null; //每部分判决结果
            boolean[] judgementMatched = null; //每部分判决结果是否有对应
            if(judgement.contains("处")) {
                judgementPunishments = (judgement.substring(judgement.indexOf("处")+1, judgement.length()-1)).split("、");
                judgementMatched = new boolean[judgementPunishments.length];
            }

            for(int i=0; i<judgementPunishments.length; i++) {
                for(int j=0; j<crimePrincipalPunishmentType.length; j++) {
                    if(judgementMatched[i] == false && judgementPunishments[i].contains(crimePrincipalPunishmentType[j])) {
                        String judgeLevel = judgementPunishments[i].substring(judgementPunishments[i].indexOf(crimePrincipalPunishmentType[j])+crimePrincipalPunishmentType[j].length(), judgementPunishments[i].length());
                        for(int k=0; k<principalPunishmentList.size(); k++) {
                            if(principalPunishmentList.get(k).contains(crimePrincipalPunishmentType[j])) {
                                String punishLevel = principalPunishmentList.get(k).substring(0, principalPunishmentList.get(k).indexOf(crimePrincipalPunishmentType[j]));
                                System.out.println(punishLevel+", "+judgeLevel);
                                if(isMatch(judgeLevel, punishLevel)) {
                                    judgementMatched[i] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                for(int j=0; j<crimeSupplementaryPunishmentType.length; j++) {
                    if(judgementMatched[i] == false && judgementPunishments[i].contains(crimeSupplementaryPunishmentType[j])) {
                        String judgeLevel = judgementPunishments[i].substring(judgementPunishments[i].indexOf(crimeSupplementaryPunishmentType[j])+crimeSupplementaryPunishmentType[j].length(), judgementPunishments[i].length());
                        for(int k=0; k<supplementaryPunishmentList.size(); k++) {
                            if(supplementaryPunishmentList.get(k).contains(crimeSupplementaryPunishmentType[j])) {
                                String punishLevel = supplementaryPunishmentList.get(k).substring(0, supplementaryPunishmentList.get(k).indexOf(crimeSupplementaryPunishmentType[j]));
                                System.out.println(punishLevel+", "+judgeLevel);
                                if(isMatch(judgeLevel, punishLevel)) {
                                    judgementMatched[i] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                for(int j=0; j<punishmentExecutionType.length; j++) {
                    if(judgementMatched[i] == false && judgementPunishments[i].contains(punishmentExecutionType[j])) {
                        String judgeLevel = judgementPunishments[i].substring(judgementPunishments[i].indexOf(punishmentExecutionType[j])+punishmentExecutionType[j].length(), judgementPunishments[i].length());
                        for(int k=0; k<punishmentExecutionList.size(); k++) {
                            if(punishmentExecutionList.get(k).contains(punishmentExecutionType[j])) {
                                String punishLevel = punishmentExecutionList.get(k).substring(0, punishmentExecutionList.get(k).indexOf(punishmentExecutionType[j]));
                                System.out.println(punishLevel+", "+judgeLevel);
                                if(isMatch(judgeLevel, punishLevel)) {
                                    judgementMatched[i] = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            for(int i=0; i<judgementPunishments.length; i++) {
                if(judgementMatched[i] == false) {
                    matchResultModel = new MatchResultModel(false, "该判决没有对应的来源："+judgementPunishments[i]);
                    return matchResultModel;
                }
            }
            matchResultModel = new MatchResultModel(true, "");
            return matchResultModel;
        }


    }


    /**获取引用法条的前后件结果
     *
     * @param referredArticles 引用的法条名称列表
     * @return 法条的前后件结果（因为一个法条可能对应多个前后件结果，所以返回列表形式）
     */
    public static List<PreAndAfterModel> getArticleContent(List<String> referredArticles) {
        List<PreAndAfterModel> resultList = new ArrayList<PreAndAfterModel>(); //结果列表

        for(int i=0; i<referredArticles.size(); i++) {
            String referredArticle = referredArticles.get(i);

            int leftBookMarkIndex = referredArticle.indexOf('《');  //引用法条中左书名号的位置
            int rightBookMarkIndex = referredArticle.indexOf('》'); //引用法条中右书名号的位置
            String lawName = "《"+referredArticle.substring(leftBookMarkIndex + 1, rightBookMarkIndex)+"》"; //获取引用法条对应的法律名

            //在查询引用法条的内容时，起作用的部分是条、款、项（编号、章号、节号一般无用）
            String articleNo = null;
            String paragraphNo = null;
            String subParagraphNo = null;
            if (referredArticle.contains("条")) { //得到引用法条对应的条号
                String tempParts = referredArticle.split("条")[0];
                articleNo = tempParts.substring(tempParts.lastIndexOf("第") , tempParts.length())+"条";
                if(!referredArticle.endsWith("条") && referredArticle.charAt(referredArticle.indexOf("条")+1) == '之') {
                    articleNo += (referredArticle.charAt(referredArticle.indexOf("条")+1)+"") + (referredArticle.charAt(referredArticle.indexOf("条")+2)+"");
                }
            }
            if (referredArticle.contains("款")) { //得到引用法条对应的款号
                String tempParts = referredArticle.split("款")[0];
                paragraphNo = tempParts.substring(tempParts.lastIndexOf("第") , tempParts.length())+"款";
            }
            if (referredArticle.contains("项")) { //得到引用法条对应的项号
                String tempParts = referredArticle.split("条")[0];
                subParagraphNo = tempParts.substring(tempParts.lastIndexOf("第") , tempParts.length())+"项";
                //这里要看数据库中的项序号是否保存“（）”，决定是否需要删掉前后的括号
            }

            if (paragraphNo == null) { //如果没有显式的款号，那么默认为第一款
                paragraphNo = "第一款";
            }
            System.out.println(lawName+", "+articleNo+", "+paragraphNo+", "+subParagraphNo);

            String fileName = "txtSrc/前后件结果-总-自动生成.txt";
            try {
                File readFile = new File(fileName);  //要读取的文件
                BufferedReader bReader = new BufferedReader(new FileReader(readFile));
                String lineStr;
                while ((lineStr = bReader.readLine()) != null) {
                    //System.out.println(lineStr);
                    String[] parts = lineStr.split("\t");
                    if (parts[0].equals(lawName) && parts[4].equals(articleNo) && parts[5].equals(paragraphNo)) {
                        //这里的判断依据应该是看数据库里该款下面有没有对应的项，对应的项有没有前后件记录
                        //如果有项的记录，即使没有显式引用，也应该加入前后件结果列表里面
                        if (subParagraphNo == null) {
                            PreAndAfterModel paam = new PreAndAfterModel(parts[6], parts[7]);
                            resultList.add(paam);
                        }
                        else {
                            if (parts[6].equals(subParagraphNo)) {
                                PreAndAfterModel paam = new PreAndAfterModel(parts[7], parts[8]);
                                resultList.add(paam);
                            }
                        }
                    }
                }
                bReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(int i=0; i<resultList.size(); i++) {
            System.out.println(resultList.get(i).getAntecedent() + ", " + resultList.get(i).getConsequent());
        }
        return resultList;
    }

    /**判断判决与法条规定的惩罚是否匹配
     *
     * @param judgementStr 判决语句
     * @param punishmentStr 法条规定的惩罚语句
     * @return 匹配结果
     */
    public static boolean isMatch(String judgementStr, String punishmentStr) {
        if(punishmentStr.contains("以上") && punishmentStr.contains("以下")) {
            String punishMinBound = punishmentStr.substring(0, punishmentStr.indexOf("以上"));
            String punishMaxBound = punishmentStr.substring(punishmentStr.indexOf("以上")+2, punishmentStr.indexOf("以下"));
            if(compareCharacterTime(judgementStr, punishMinBound) > 0 && compareCharacterTime(judgementStr, punishMaxBound) < 0) {
                return true;
            }
        }
        else if(punishmentStr.contains("以上") && !punishmentStr.contains("以下")) {
            String punishMinBound = punishmentStr.substring(0, punishmentStr.indexOf("以上"));
            if(compareCharacterTime(judgementStr, punishMinBound) > 0) {
                return true;
            }
        }
        else if(!punishmentStr.contains("以上") && punishmentStr.contains("以下")) {
            String punishMaxBound = punishmentStr.substring(0, punishmentStr.indexOf("以下"));
            if(compareCharacterTime(judgementStr, punishMaxBound) < 0) {
                return true;
            }
        }
        else if(punishmentStr.contains("可以") || punishmentStr.contains("应当") || punishmentStr.contains("应")) {
            return true;
        }
        return false;
    }


    /**比较判决定刑与法条规定定刑的时间（中文表示）
     *
     * @param judgeTime 判决定刑的时间
     * @param punishTime 法条规定定刑的时间
     * @return 比较值的大小，比较值小于0， 则判决定刑的时间小于法条规定定刑的时间；比较值大于0，则判决定刑的时间大于法条规定定刑的时间；比较值等于0，则判决定刑的时间等于法条规定定刑的时间；
     */
    public static int compareCharacterTime(String judgeTime, String punishTime) {
        String timePattern = "(.*)年(.*)";
        Pattern r = Pattern.compile(timePattern);

        Matcher m1 = r.matcher(judgeTime);
        Matcher m2 = r.matcher(punishTime);
        if (m1.matches() && m2.matches()) { //judgeTime和punishTime都有XX年
            String judgeYear = m1.group(1);
            String punishYear = m2.group(1);
            return Utility.characterToInt(judgeYear) - Utility.characterToInt(punishYear);
        }
        else if(m1.matches() && !m2.matches()) { //judgeTime有XX年，punishTime没有XX年
            return 1;
        }
        else if(!m1.matches() && m2.matches()) { //judgeTime没有XX年，punishTime有XX年
            return -1;
        }
        else{ //judgeTime和punishTime都没有XX年
            return Utility.characterToInt(judgeTime) - Utility.characterToInt(punishTime);
        }
    }


    public static void main(String[] args) {
        ArrayList<String> referredArticles = new ArrayList<String>();
        referredArticles.add("《中华人民共和国刑法》第一百三十三条");
        referredArticles.add("《中华人民共和国刑法》第六十七条第三款");
        referredArticles.add("《中华人民共和国刑法》第七十二条第一款");
        //referredArticles.add("《中华人民共和国刑法》第一百三十三条之一");
        //referredArticles.add("《中华人民共和国刑法》第五十二条");
        //referredArticles.add("《中华人民共和国刑法》第五十三条");
        MatchResultModel matchResultModel = verifyRationality("被告人韩某违反道路交通管理法规，驾驶机件不符合技术标准的机动车，发生交通事故，致一人死亡，其行为已构成交通肇事罪。公诉机关指控的罪名成立，应予支持。鉴于被告人韩某能如实供述自己的罪行，依法可对其从轻处罚，同时本案民事部分双方已经达成了赔偿协议，赔偿款已经全部付清，也得到了被害人家属的谅解，也可酌情从轻处罚，对其适用缓刑也不致再危害社会，可依法适用缓刑",
                "被告人韩某犯交通肇事罪，判处有期徒刑一年二个月、缓刑二年" , referredArticles);
        //MatchResultModel matchResultModel = verifyRationality("被告人王某某醉酒驾驶机动车，行为构成危险驾驶罪", "被告人王某某犯危险驾驶罪，判处拘役三个月，并处罚金人民币九千元", referredArticles);
        if(matchResultModel.isMatch()) {
            System.out.println("匹配！");
        }
        else{
            System.out.println(matchResultModel.getReason());
        }
    }

}
