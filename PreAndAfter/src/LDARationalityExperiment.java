import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import java.io.*;

public class LDARationalityExperiment {

    public static void readFilesAndSearRelatedLaws() {
        String fileName = "txtSrc/刑法前后件结果-自动生成.txt";
        try {
            File readFile = new File(fileName);  //要读取的文件
            BufferedReader bReader = new BufferedReader(new FileReader(readFile));
            String lineStr; //该行的文本内容
            int lineNo = 0; //该行对应的行号，在LDA训练模型中，行号即下标
            while ((lineStr = bReader.readLine()) != null) {
                //System.out.println(lineStr);
                String[] parts = lineStr.split("\t");
                String antecedent = parts[parts.length-2]; //分隔开的数组的倒数第二项为法律前件
                String experimentParameter = "";
                if(antecedent.length() >= 2) { //取法律前件的一部分作为传递给python进行查找相似句子订单参数
                    experimentParameter = antecedent.substring(1, antecedent.length() - 1);
                }
                else {
                    experimentParameter = antecedent;
                }

                //模拟使用控制台运行python程序
                String[] arguments = new String[] { "python", "pythonSrc/useLdaDeal.py", experimentParameter};
                try {
                    Process process = Runtime.getRuntime().exec(arguments);
                    InputStreamReader stdin = new InputStreamReader(process.getInputStream());
                    LineNumberReader in = new LineNumberReader(stdin);
                    String resultStr = null;
                    while ((resultStr = in.readLine()) != null) {
                        System.out.println(resultStr);
                        resultStr = resultStr.substring(1, resultStr.length()-1); //因为输出是列表的形式，所以需要删除前后的中括号
                        //将输出句中的()替换为[]，否则split方法的参数报错
                        resultStr = resultStr.replace("(", "[");
                        resultStr = resultStr.replace(")", "]");
                        String[] similarSentences = resultStr.split("], "); //列表中的对象是键值对的形式，所以不能单纯的以逗号作为分隔符
                        double accuracy = 0; //相似句的准确度
                        for(int i=0; i<similarSentences.length; i++) {
                            similarSentences[i] = similarSentences[i].replace("[", "");
                            similarSentences[i] = similarSentences[i].replace("]", "");
                            int sentenceIndex = Integer.parseInt(similarSentences[i].split(", ")[0]);
                            if(sentenceIndex == lineNo) { //如果给出的相似句的下标和本行行号对应
                                double similarPercent = Double.parseDouble(similarSentences[i].split(", ")[1]);
                                accuracy = ( (similarSentences.length-i) * 1.0 / similarSentences.length ) * similarPercent;
                                break;
                            }
                        }
                        System.out.println("实验句："+experimentParameter+"，相关法条匹配准确度："+accuracy);
                    }
                    in.close();
                    int procResult = process.waitFor();
                    if(procResult > 0) {
                        System.out.println("执行出错。错误编码：" + procResult);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                lineNo += 1;
            }
            bReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readFilesAndSearRelatedLaws();
    }
}
