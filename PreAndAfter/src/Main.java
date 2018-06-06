import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    /*全局变量*/
    public String lawName = "《中华人民共和国刑事诉讼法》"; //法律名
    public String partNo = "第一编";		   //编序号
    public String chapterNo = "第一章";      //章序号
    public String sectionNo = "第一节";      //节序号
    public String articleNo = "第一条";      //条序号
    public String paragraphNo = "第一款";    //款序号
    public String subParagraphNo = "第一项"; //项序号
    public String itemNo = "第一目";         //目序号

    public char subParagraphType; //项的类型，"Q"表示项中内容是款的前件，"H"表示项中内容是款的后件
    public char andOrOneOf; //项是“同时”还是“之一”，"A"表示同时，"O"表示之一
    public String preStr; //（可能是）多个项共用的一个前件，
    public String afterStr; //（可能是）多个项共用的一个后件
    List<PreAndAfterModel> paaResultList; //保存分离前后件的结果（如果项本身不具有前后件关系，那么多个项的前件或后件可能是共用的）

    public static void main(String[] args) {
        Main mainClass = new Main();
        mainClass.readAndDealTxtFile("txtSrc/中华人民共和国刑事诉讼法.txt");
    }


    /**读取TXT文件内容，并对每一行文件内容进行逐一处理
     *
     * @param fileName 文件名
     */
    public void readAndDealTxtFile(String fileName) {
        String lineStr = ""; //记录文件每一行内容的字符串
        String dataContent = ""; //写进前后件结果的每一行数据内容

        try {
            File readFile = new File(fileName);  //要读取的文件
            BufferedReader bReader = new BufferedReader(new FileReader(readFile));

            File writeFile = new File("txtSrc/前后件结果-总-自动生成.txt"); //要写入的文件
            if(!writeFile.exists()){
                writeFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(writeFile,true); //第二个参数为true表示以追加形式写文件，false表示删除原有文件内容、重新写入
            BufferedWriter bWriter= new BufferedWriter(fileWriter);

            while((lineStr = bReader.readLine()) != null) {
            	//System.out.println(lineStr);
                String levelStr = changeNos(lineStr); //解析该行内容，改变全局变量的值，返回该行内容对应的等级
                if(levelStr == "AR" || levelStr == "PG") { //如果该行内容是条（即XX第一款）或款（即XX除第一款外的其他款）
                    if(levelStr == "AR") { //如果是条，删除等级序号，只保留后面的内容，即为该条第一款的内容
                        lineStr = lineStr.split(" ")[1];
                    }
                    if(lineStr.contains("前款")) { //如果该行内容有隐式引用其他款的情况，需要把引用的“前款”显式转化为该款前一款的款序号
                        String thisParagraphNoStr = (paragraphNo.split("第")[1]).split("款")[0]; //获取本款的中文编号（因为已经改变过全局变量，所以paragraphNo中就是本款的款序号）
                        int thisParagraphNo = Utility.characterToInt(thisParagraphNoStr); //将本款的中文编号转为数字
                        String lastParagraphNoStr = Utility.intToCharacter(thisParagraphNo-1); //前一款的数字编号即为本款的数字编号减一，然后转为中文编号
                        lineStr = lineStr.replaceAll("前款", articleNo+"第"+lastParagraphNoStr+"款");
                    }
                    if(lineStr.contains("前两款")) { //如果该行内容有隐式引用其他款的情况，需要把引用的“前两款”显式转化为该款前两款的款序号
                        String thisParagraphNoStr = (paragraphNo.split("第")[1]).split("款")[0]; //获取本款的中文编号（因为已经改变过全局变量，所以paragraphNo中就是本款的款序号）
                        int thisParagraphNo = Utility.characterToInt(thisParagraphNoStr); //将本款的中文编号转为数字
                        String lastParagraphNoStr = Utility.intToCharacter(thisParagraphNo-1); //前一款的数字编号即为本款的数字编号减一，然后转为中文编号
                        String last2ParagraphNoStr = Utility.intToCharacter(thisParagraphNo-2);//前两款的数字编号即为本款的数字编号减二，然后转为中文编号
                        lineStr = lineStr.replaceAll("前两款", articleNo+"第"+lastParagraphNoStr+"款"+"，"+articleNo+"第"+last2ParagraphNoStr+"款");
                    }
                    if(lineStr.contains("前三款")) { //如果该行内容有隐式引用其他款的情况，需要把引用的“前三款”显式转化为该款前三款的款序号
                        String thisParagraphNoStr = (paragraphNo.split("第")[1]).split("款")[0]; //获取本款的中文编号（因为已经改变过全局变量，所以paragraphNo中就是本款的款序号）
                        int thisParagraphNo = Utility.characterToInt(thisParagraphNoStr); //将本款的中文编号转为数字
                        String lastParagraphNoStr = Utility.intToCharacter(thisParagraphNo-1); //前一款的数字编号即为本款的数字编号减一，然后转为中文编号
                        String last2ParagraphNoStr = Utility.intToCharacter(thisParagraphNo-2);//前两款的数字编号即为本款的数字编号减二，然后转为中文编号
                        String last3ParagraphNoStr = Utility.intToCharacter(thisParagraphNo-3);//前两款的数字编号即为本款的数字编号减二，然后转为中文编号
                        lineStr = lineStr.replaceAll("前三款", articleNo+"第"+lastParagraphNoStr+"款"+"，"+articleNo+"第"+last2ParagraphNoStr+"款"+"，"+articleNo+"第"+last3ParagraphNoStr+"款");
                    }
                    String[] sentences = seperateSentences(lineStr); //获取该款分隔得到的句子数组
                    for(int i=0; i<sentences.length; i++){ //对每一个句子，找前后件
                        paaResultList = findPreAndAfter(sentences[i]);//对一个句子来说，可能有多对前后件，所以用列表表示找到的前后件结果
                        for (int j = 0; j < paaResultList.size(); j++) {
                            //这里要注意删除前后件中结尾的标点符号
                            dataContent = lawName + "\t" + partNo + "\t" + chapterNo + "\t" + sectionNo + "\t" + articleNo + "\t" + paragraphNo + "\t" + paaResultList.get(j).getAntecedent() + "\t" + paaResultList.get(j).getConsequent() + "\r\n";
                            //System.out.println(dataContent);
                            bWriter.write(dataContent);
                        }
                    }
                }
                else if(levelStr == "SP" || levelStr == "IT") { //如果该行内容是项或目，统一作为所在款的前件或者后件存在
                    if(levelStr == "SP") {
                    	int rightParenthesesIndex = lineStr.indexOf('）'); //获取右括号的位置
                    	lineStr = lineStr.substring(rightParenthesesIndex+1, lineStr.length()); //删除等级序号，只保留后面的内容

                        //这里不判断项本身有没有前后件，简单地认为项本身没有前后件关系
                        if(subParagraphType == 'Q') { //项作为前件存在
                            if(andOrOneOf == 'O') { //项是“之一”，那么每一项都对应一条前后件记录
                                //前件的内容是共有前件加上本行内容，并删除前后件结尾的标点符号
                                dataContent = lawName + "\t" + partNo + "\t" + chapterNo + "\t" + sectionNo + "\t" + articleNo + "\t" + paragraphNo + "\t" + subParagraphNo + "\t" + (preStr+lineStr).substring(0, preStr.length()+lineStr.length()-1) + "\t" + afterStr + "\r\n";
                                System.out.println(dataContent);
                                bWriter.write(dataContent);
                            }
                            else { //项是“同时”，那么所有的项加在一起，对应其所属款的一条前后件记录
                                preStr += lineStr.substring(0, lineStr.length()-1)+"，"; //把项的内容删除标点符号后加在共有前件中
                                if(lineStr.endsWith("。")) { //如果项以中文句号结尾，那么说明所有的项都已经结束，记录一条数据
                                    //删除前后件结尾的标点符号
                                    dataContent = lawName + "\t" + partNo + "\t" + chapterNo + "\t" + sectionNo + "\t" + articleNo + "\t" + paragraphNo + "\t" +  preStr.substring(0, preStr.length()-1) + "\t" + afterStr.substring(0, afterStr.length()-1) + "\r\n";
                                    System.out.println(dataContent);
                                    bWriter.write(dataContent);
                                }
                            }
                        }
                        else if(subParagraphType == 'H') {//项作为后件存在
                            if(andOrOneOf == 'O') { //项是“之一”，那么每一项都对应一条前后件记录
                                //后件的内容是共有后件加上本行内容，并删除前后件结尾的标点符号
                                dataContent = lawName + "\t" + partNo + "\t" + chapterNo + "\t" + sectionNo + "\t" + articleNo + "\t" + paragraphNo + "\t" + subParagraphNo + "\t" + preStr + "\t" + (afterStr+lineStr).substring(0, afterStr.length()+lineStr.length()-1) + "\r\n";
                                System.out.println(dataContent);
                                bWriter.write(dataContent);
                            }
                            else { //项是“同时”，那么所有的项加在一起，对应其所属款的一条前后件记录
                                afterStr += lineStr.substring(0, lineStr.length()-1)+"，"; //把项的内容删除标点符号后加在后件中
                                if(lineStr.endsWith("。")) { //如果项以中文句号结尾，那么说明所有的项都已经结束，记录一条数据
                                    //删除前后件结尾的标点符号
                                    dataContent = lawName + "\t" + partNo + "\t" + chapterNo + "\t" + sectionNo + "\t" + articleNo + "\t" + paragraphNo + "\t" +  preStr.substring(0, preStr.length()-1) + "\t" + afterStr.substring(0, afterStr.length()-1) + "\r\n";
                                    System.out.println(dataContent);
                                    bWriter.write(dataContent);
                                }
                            }
                        }
                    }
                    else {
                    	int periodIndex = lineStr.indexOf("."); //获取点的位置
                        lineStr = lineStr.substring(periodIndex+1, lineStr.length()-1); //删除等级序号，只保留后面的内容

                        //目如何处理，后期待补充
                    }

                }
            }

            bReader.close();
            bWriter.close();
            fileWriter.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**改变全局变量（一系列序号）的值
     *
     * @param content 内容
     * @return 改变的是哪种全局变量的值，即这条内容对应的是哪个等级（PA代表编，CH代表章，SE代表节，AR代表条，PG代表款，SP代表项，IT代表目）；
     *          若返回为空白字符串，则说明这条内容为空白；若返回为ERROR，则说明出现错误
     */
    public String changeNos(String content) {
        if (content.trim().equals("")) {//如果是空白行，直接返回
            return "";
        }
        else if (content.startsWith("第")) { //该行内容是编、章、节、条之一
            String levelStr = content.split(" ")[0]; //将这条内容以空格符进行分离，分离数组的第一部分即为所在的等级

            if (levelStr.contains("编")) { //新的一编，下属的章序号从一开始
                partNo = levelStr;
                chapterNo = "第一章";
                return "PA";
            }
            else if (levelStr.contains("章")) { //新的一章，下属的节序号从一开始
                chapterNo = levelStr;
                sectionNo = "第一节";
                return "CH";
            }
            else if (levelStr.contains("节")) { //新的一节，下属的条序号不需要改变
                sectionNo = levelStr;
                return "SE";
            }
            else if (levelStr.contains("条")) { //新的一条，那么也是该条对应的第一款
                articleNo = levelStr;
                paragraphNo = "第一款";
                return "AR";
            }
            else { //这条内容以“第”开头，却不含“编、章、节、条”任意之一，输出错误信息
                System.out.println("错误！本行内容无法判断所在的等级！内容为：" + content);
                return "ERROR";
            }
        }
        else { //该行内容是款、项、目之一
            if (content.startsWith("（")) { //如果这条内容以中文括号开头，那么说明是项
                int rightParenthesesIndex = content.indexOf('）'); //获取右括号的位置
                subParagraphNo = "第" + content.substring(0, rightParenthesesIndex+1) + "项"; //将这条内容右括号及之前的部分算作项的编号
                return "SP";
            }
            else if (content.startsWith("0") || content.startsWith("1") || content.startsWith("2") || content.startsWith("3") || content.startsWith("4")
                    || content.startsWith("5") || content.startsWith("6") || content.startsWith("7") || content.startsWith("8") || content.startsWith("9")) {
                //如果这条内容以阿拉伯数字开头，那么说明是目
                int periodIndex = content.indexOf("."); //获取点的位置
                itemNo = "第" + content.substring(0, periodIndex)+ "条"; //将这条内容点之前的部分算作目的编号
                return "IT";
            }
            else { //如果这条内容不是项或者目，那么说明是款
                String lastParagraphNoStr = (paragraphNo.split("第")[1]).split("款")[0]; //获取上一个款的中文编号
                int lastParagraphNo = Utility.characterToInt(lastParagraphNoStr); //将上一个款的中文编号转为数字
                String thisParagraphNoStr = Utility.intToCharacter(lastParagraphNo+1); //本款的数字编号即为上一款的数字编号加一，然后转为中文编号
                paragraphNo = "第" + thisParagraphNoStr + "款";
                return "PG";
            }
        }
    }


    /**对于一句话，寻找它的前件和后件
     *
     * @param sentence 一句话
     * @return 前件与后件，以列表表示
     */
    public List<PreAndAfterModel> findPreAndAfter(String sentence) {
        
    	List<PreAndAfterModel> resultList = new ArrayList<PreAndAfterModel>();
    	PreAndAfterModel paam = new PreAndAfterModel();
        
        if(sentence.contains("下列") || sentence.contains("如下")) { //如果这句话有下属项
            String[] commaParts = sentence.split("，"); //用逗号分隔这个句子
            for(int i=0; i<commaParts.length; i++) {
                if(commaParts[i].contains("下列") || commaParts[i].contains("如下")) {
                    preStr = "";
                    afterStr = "";

                    if(commaParts[i].endsWith("的")) { //“下列”、“如下”和“的，”在一个句子中，其下属的项作为一部分前件存在
                        subParagraphType = 'Q';
                        if(commaParts[i].contains("同时")) { //项需要同时满足，那么下属所有的项保存在“款”级，都作为款的前件的一部分
                            andOrOneOf = 'A';
                        }
                        else { //项不需要同时满足，那么下属的项分开保存在“项”级，分别是项的前件的一部分
                            andOrOneOf = 'O';
                        }

                        //将这句话之前的部分作为其他共有前件
                        for(int j=0; j<i; j++) {
                            preStr += commaParts[j]+"，";
                        }
                        //将这句话之后的部分作为其他共有后件
                        for(int j=i+1; j<commaParts.length; j++) {
                            afterStr += commaParts[j]+"，";
                        }
                    }
                    else if(commaParts[i].contains("应当") || commaParts[i].contains("可以")) { //“下列”、“如下”和“应当”、“可以”在一个句子中，其下属的项作为一部分后件存在
                        subParagraphType = 'H';
                        if(commaParts[i].contains("同时")) { //项需要同时满足，那么下属所有的项保存在“款”级，都作为款的后件的一部分
                            andOrOneOf = 'A';
                        }
                        else { //项不需要同时满足，那么下属的项分开保存在“项”级，分别是项的后件的一部分
                            andOrOneOf = 'O';
                        }

                        //将这句话之前的部分作为其他共有前件
                        for(int j=0; j<i; j++) {
                            preStr += commaParts[j]+"，";
                        }
                        //将这句话及之后的部分作为其他共有后件
                        for(int j=i; j<commaParts.length; j++) {
                            afterStr += commaParts[j]+"，";
                        }
                    }
                    else if (commaParts[i].startsWith("对于") || commaParts[i].startsWith("对") ||  //“下列”、“如下”和“对于”、“对”在一个句子中，其下属的项作为前件存在
                            commaParts[i].contains("是指") || commaParts[i].contains("，是") || commaParts[i].contains("，为") || //下定义的情况，其下属的项作为前件存在)
                            commaParts[i].contains("的种类")) { //列举的情况，其下属的项作为前件存在
                    	subParagraphType = 'Q';
                        andOrOneOf = 'O'; //项不需要同时满足，那么下属的项分开保存在“项”级，分别是项的前件的一部分

                        //将这句话之前的部分作为其他共有前件
                        for(int j=0; j<=i; j++) {
                            preStr += commaParts[j]+"，";
                        }
                        //将这句话之后的部分作为其他共有后件
                        for(int j=i+1; j<commaParts.length; j++) {
                            afterStr += commaParts[j]+"，";
                        }
                    }
                    else {//找不到其他关键词，那么其下属的项作为后件存在，这个款的叙述作为前件存在
                        subParagraphType = 'H';
                        andOrOneOf = 'O'; //项不需要同时满足，那么下属的项分开保存在“项”级，分别是项的前件的一部分

                        //将整个款的部分作为共有前件
                        for(int j=0; j<commaParts.length; j++) {
                            preStr += commaParts[j]+"，";
                        }
                    }

                    if(preStr.endsWith("：，")) {
                        preStr = preStr.substring(0, preStr.length() - 1); //删除共有前件结尾的冒号逗号组合，只保留冒号
                    }
                    if(afterStr.endsWith("：，")) {
                        afterStr = afterStr.substring(0, afterStr.length() - 1); //删除共有后件结尾的冒号逗号组合，只保留冒号
                    }

                    paam = new PreAndAfterModel(preStr, afterStr);
                }
            }
            resultList.add(paam);
        }
        else { //如果这句话没有下属项
            if (sentence.contains("的，")) {
            	//需要判断是否有多个“的，”的情况
                if(sentence.split("的，").length > 2) { //如果分割之后的结果长度大于2，说明存在多个“的，”

                    //“的”之前的句子作为前件，“的”之后的句子作为后件
                    //“处”、“应当”、“可以”、“依照”、“不得”这种关键词作为后件的开端
                    //“或者”、“并”之后的句子和上一个句子的类型一致

                	String[] tempStrArr = sentence.split("，"); // 将整句话重新以逗号进行分隔
                	
                	boolean isComplete = false; //记录是否已经完成一个前后件的分离
                    boolean meetDe = false; //记录是否已经遇到“的”这个关键词
                    char lastSentenceType = ' '; //记录上一个句子的类型，Q表示前件，H表示后件
                    String preString = ""; //保存前件的字符串
                	String afterString = ""; //保存后件的字符串
                	
                	for(int i=0; i<tempStrArr.length-1; i++) {
                        if(tempStrArr[i].endsWith("的")) { //如果这句话以“的”结尾
                            preString += tempStrArr[i] + "，";
                            lastSentenceType = 'Q';
                            meetDe = true;
                            isComplete = false;
                        }
                		else if(tempStrArr[i].startsWith("处") || tempStrArr[i].startsWith("可以") || tempStrArr[i].startsWith("应当")  ||
                                tempStrArr[i].startsWith("依照") || tempStrArr[i].startsWith("不得")){ //如果这句话以典型的后件开端关键词开头，说明这句话是后件的开端
                			afterString += tempStrArr[i] + '，';
                            lastSentenceType = 'H';
                            if(tempStrArr[i+1].startsWith("但是") || tempStrArr[i].endsWith("的")) { //如果下一句话以“但是”开头，或者以“的”结尾，说明这句话是后件的结尾
                                isComplete = true;
                            }
                		}
                        else if(tempStrArr[i].startsWith("或者") || tempStrArr[i].startsWith("并")) { //如果这句话以“或者”或“并且”开头，说明这句话和上一句话一个等级
                            if(lastSentenceType == 'Q') { //上一句话是前件，那么这句话也是前件
                                preString += tempStrArr[i]+"，";
                            }
                            else{ //上一句话是后件，那么这句话也是后件
                                afterString += tempStrArr[i] + '，';
                                if(tempStrArr[i+1].startsWith("但是") || tempStrArr[i].endsWith("的")) { //如果下一句话以“但是”开头，或者以“的”结尾，说明这句话是后件的结尾
                                    isComplete = true;
                                }
                            }
                        }
                        else {//如果这句话什么关键词都没有
                            if(!meetDe) {  //如果这句话之前还没有“的”这个关键词，那么这句话作为前件的一部分
                                preString += tempStrArr[i] + "，";
                                lastSentenceType = 'Q';
                            }
                            else { //如果这句话之前已经有“的”这个关键词，那么这句话作为后件
                                afterString += tempStrArr[i] + '，';
                                lastSentenceType = 'H';
                            }
                        }

                		if(isComplete) {//如果已经找到一对前后件，那么将这对前后件加入结果列表中
                            meetDe = false;
                			paam = new PreAndAfterModel(preString, afterString);
                			resultList.add(paam);
                			preString = "";
                			afterString = "";
                		}
                	}
                    //把分隔句子的最后一句话加入到后件中，将这对前后件加入结果列表中
                    afterString += tempStrArr[tempStrArr.length-1] + '，';
                    paam = new PreAndAfterModel(preString, afterString);
                    resultList.add(paam);
                    preString = "";
                    afterString = "";

                }
                else { //如果分割之后的结果长度等于2，说明只有一个“的，”
                	String[] splitParts = sentence.split("的，");
                	paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                	resultList.add(paam);
                }
            } 
            else if (sentence.contains("应当")) {
            	String[] splitParts = sentence.split("应当");
                if (splitParts[0].endsWith("，")) { //如果前件以逗号结尾，删除结尾的逗号
                	splitParts[0] = splitParts[0].substring(0, splitParts[0].length()-1);
                }
                splitParts[1] = "应当" + splitParts[1]; //把“应当”加入后件，与“可以”相区别
                
                paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                resultList.add(paam);
            } 
            else if (sentence.contains("可以")) {
            	String[] splitParts = sentence.split("可以");
                if (splitParts[0].endsWith("，")) { //如果前件以逗号结尾，删除结尾的逗号
                	splitParts[0] = splitParts[0].substring(0, splitParts[0].length()-1);
                }
                splitParts[1] = "可以" + splitParts[1]; //把“可以”加入后件，与“可以”相区别
                
                paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                resultList.add(paam);
            } 
            else if (sentence.startsWith("对于") || sentence.startsWith("对")) {
                int firstCommaIndex = sentence.indexOf("，"); //找到第一个逗号出现的位置
                if(firstCommaIndex != -1) {
                	paam = new PreAndAfterModel(sentence.substring(0, firstCommaIndex), 
                	                        sentence.substring(firstCommaIndex + 1, sentence.length())); //第一个逗号前面的部分为前件，第一个逗号后面的部分为后件
                	resultList.add(paam);
                }
            } 
            else if (sentence.contains("是指") || sentence.contains("，是") || sentence.contains("，为")) {//下定义的情况
            	if(sentence.contains("是指")) {
            		String[] splitParts = sentence.split("是指");
            		if (splitParts[0].endsWith("，")) { //如果前件以逗号结尾，删除结尾的逗号
                    	splitParts[0] = splitParts[0].substring(0, splitParts[0].length()-1);
                    }
            		
            		paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                    resultList.add(paam);
            	}
            	else if(sentence.contains("，是")) {
            		String[] splitParts = sentence.split("，是");
            		paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                    resultList.add(paam);
            	}
            	else { //“，为”的情况需要判断是“is”还是“for”的意思
            		boolean meaningFor = sentence.contains("为自己") || sentence.contains("为其") || sentence.contains("为他人"); //判断“为”是否作为“for”的意思存在
            		if(!meaningFor) { //“为”不是“for”，而是“is”
            			String[] splitParts = sentence.split("，为");
                		paam = new PreAndAfterModel(splitParts[0], splitParts[1]);
                        resultList.add(paam);
            		}
            	}
            }
        }
        /*
        for(int j=0; j<resultList.size(); j++) {
        	System.out.println(resultList.get(j).getAntecedent()+", "+resultList.get(j).getConsequent());
        }*/
        
        return resultList;
    }


    /**分离某一款中的句子（以中文句号、分号划分一句）
     *
     * @param paragraphContent 款的内容
     * @return 该款分离出来的句子数组
     */
    public String[] seperateSentences(String paragraphContent){
        String[] periodParts = paragraphContent.split("。"); //保存以句号分隔的字符串数组
        String[] semicolonParts = {}; //保存以分号分隔的字符串数组
        int semicolonPartsIndex = 0; //每次添加新的内容时，数组开始的位置
        for(int i=0; i<periodParts.length; i++) {
            String[] tempStrArr = periodParts[i].split("；");
            semicolonParts = Arrays.copyOf(semicolonParts, semicolonParts.length + tempStrArr.length); //数组扩容
            System.arraycopy(tempStrArr, 0, semicolonParts, semicolonPartsIndex, tempStrArr.length); //将新数组的内容复制到以分号分隔的字符串数组中
            semicolonPartsIndex += tempStrArr.length; //更新每次添加时的开始位置
        }
        /*for(int j=0; j<semicolonParts.length; j++) {
        	System.out.println(semicolonParts[j]+"    ");
        }*/
        return semicolonParts;
    }

}
