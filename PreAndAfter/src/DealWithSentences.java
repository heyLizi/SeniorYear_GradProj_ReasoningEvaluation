import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.regex.Pattern;


public class DealWithSentences {

    /**处理一个句子，返回它的有用中文单词数组
     *
     * @param sentence 句子
     */
    public static String[] dealWithASentence(String sentence) {
        String[] words = splitSentence(sentence);
        removePunctuations(words);
        removeUnusefulWords(words);
        return words;
    }


    /**将一个中文句子中的单词提炼出数组形式
     *
     * @param sentence 中文句子
     * @return 单词数组
     */
    public static String[] splitSentence(String sentence) {

        String[] words = null; //保存返回结果的数组

        /*通过Jython在Java中调用Python函数
        p.s.一开始一直报错"Cannot create PyString with non-byte value"，意识到应该是中文字符的问题，虽然Jython所在目录并不含有中文字符，所传的参数也没有中文字符，
        但是后来发现需要导入的jar包jython-standalone.jar所在的目录含有中文字符，修改目录至英文，问题就解决了
        python执行时的sys.path和jython的sys.path路径不一致，所以还需要手动添加第三方库路径*/

        /*System.setProperty("python.home","D:\\jython\\jython2.7.0"); //设置Jython所在的目录

        PySystemState sys = Py.getSystemState(); //手动添加第三方库路径
        sys.path.add("D:\\Python\\Python2.7\\Lib");
        sys.path.add("D:\\Python\\Python2.7\\Lib\\site-packages");
        sys.path.add("D:\\Python\\Python2.7\\Lib\\site-packages\\jieba");

        String pythonFunc = "pythonSrc/useJiebaSplitSentence.py";  //设置Python函数所在目录及名称

        PythonInterpreter pipt = new PythonInterpreter(); //Python编译器

        pipt.execfile(pythonFunc); // 加载python程序

        PyFunction pyFunc = pipt.get("markWordProperty", PyFunction.class); //调用Python程序中的函数
        PyObject words = pyFunc.__call__(Py.newStringUTF8(sentence));
        System.out.println(words);
        pipt.cleanup();
        pipt.close();
        */

        /*还可以通过模拟命令行直接执行java程序来运行，然后获取控制台输出来得到python的结果
        这种要比通过Jython调用快，所以采用这种方式*/
        String[] arguments = new String[] { "python", "pythonSrc/useJiebaSplitSentence.py", sentence};
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            InputStreamReader stdin = new InputStreamReader(process.getInputStream());
            //BufferedReader in = new BufferedReader(stdin);
            LineNumberReader in = new LineNumberReader(stdin);
            String line = null;
            while ((line = in.readLine()) != null) {
                //System.out.println(line);
                line = line.substring(1, line.length()-1); //因为输出是列表的形式，所以需要删除前后的中括号
                words = line.split("], "); //列表中的对象也是列表的形式，所以不能单纯的以逗号作为分隔符
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

        return words;
    }


    /**删除句子对应的中文数组中的无用单词
     *
     * @param sentenceWords 句子对应的中文数组
     * @return 有用单词的数组
     */
    public static void removeUnusefulWords(String[] sentenceWords) {
        if(sentenceWords != null) {
            for(int i=0; i<sentenceWords.length; i++) {
                String[] parts = sentenceWords[i].split(", "); //按照逗号分隔字符串，逗号前面为单词，逗号后面为单词的词性
                if(!(parts[1].equals("n") || parts[1].equals("v") || parts[1].equals("a") || parts[1].equals("i")) ) { //只保留名词、动词、形容词、专有名词
                    sentenceWords[i] = "null, null";
                }
            }
        }
    }


    /**删除句子对应的中文数组中的标点符号
     *
     * @param sentenceWords 句子对应的中文数组
     * @return 删除标点符号后的数组
     */
    public static void removePunctuations(String[] sentenceWords) {
        if(sentenceWords != null) {
            for(int i=0; i<sentenceWords.length; i++) {
                sentenceWords[i] = sentenceWords[i].replace("\"", ""); //删除字符串中的引号
                sentenceWords[i] = sentenceWords[i].replace("[", ""); //删除字符串中的左中括号
                sentenceWords[i] = sentenceWords[i].replace("]", ""); //删除字符串中的右中括号
                String[] parts = sentenceWords[i].split(","); //按照逗号分隔字符串，逗号前面为单词，逗号后面为单词的词性
                if(parts[1].equals("x")) { //如果词性为“x”，说明是标点符号
                    sentenceWords[i] = "null, null";
                }
            }
        }
    }

    public static void main(String[] args) {
        String[] words = dealWithASentence("被告人何津持刀砍杀被害人颜某、钟某某（甲）的行为构成故意杀人罪；醉酒后在道路上驾驶机动车的行为构成危险驾驶罪，应依法予以并罚。何津因交通事故纠纷而泄愤报复，在醉酒状态下驾车到被害人住宅小区，并持砍刀闯入被害人家连杀二人，情节特别恶劣，罪行极其严重，社会危害极大，均应依法惩处");
        if(words != null) {
            for(int i=0; i<words.length; i++) {
                System.out.print(words[i]+"   ");
            }
        }
    }

}
