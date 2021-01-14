//Class Library
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JFileChooser;


public class Debuger extends JFrame
{
  static final char[] ALPHABET = IntStream.rangeClosed('A','Z').mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();
  static final char[] alphabet = IntStream.rangeClosed('a','z').mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();
  
  public static void main(String[] args)throws IOException, Exception
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    final String message[] = {"1~3の数字か、qまたはQで入力してください。","準備","中断","終了"};
    
    //モード選択
    int mode = 0;
    while(true)
    {
      System.out.println("モードを選択：1.デバック準備 2.デバック中断 3.デバック終了 q.終了");
      String modeStr = reader.readLine();

      try{
        mode = Integer.parseInt(modeStr);
        if(mode > 0 && mode < 4){
          break;
        }else{
          System.out.println(message[0]);
        }
      }catch(NumberFormatException e){
        if(modeStr.equals("q") || modeStr.equals("Q")){
          System.exit(0);
        }else{
          System.out.println(message[0]);
        }
      }

      System.out.println("");
    }

    System.out.println("");
    Debuger deb = new Debuger();

    //Pathの取得
    String filePath = deb.fileSelect();

		//言語の認識
		char[] pathArray = filePath.toCharArray();
		String extention = "";
		int lenArray = pathArray.length-1;
		for(int i = lenArray; i >= 0; i--)
		{
			if(pathArray[i] == '.'){
				for(int c = i+1; c <= lenArray; c++)
				{
					extention += String.valueOf(pathArray[c]);
				}
				break;
			}
		}

		if(extention.equals("java")){
			//ファイル内容の置き換え
    	String line = changeStr(filePath, mode);

    	//書き込み
    	PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
    	pw.print(line);
    	pw.close();

    	System.out.println("デバック" + message[mode] + "の処理を実行しました。");
		}else{
			System.out.println("Javaのソースコードを選択してください");
		}
    
		reader.close();
    System.exit(0);
  }

  public String fileSelect()
	{
    JFileChooser filechooser = new JFileChooser();

    String filePath = null;
    int select = filechooser.showOpenDialog(this);
    if(select == JFileChooser.APPROVE_OPTION){
      File file = filechooser.getSelectedFile();
      filePath = file.getAbsolutePath();
    }else if(select == JFileChooser.ERROR_OPTION){
      System.out.println("エラーが発生しました");
      System.exit(0);
    }

    return filePath;
  }

  public static String changeStr(String filePath, int mode)throws IOException, Exception
  {
    String line = "";
    String lineDec = "";
    char[] lineArray = null;

    if(filePath != null){
      BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));

      while((lineDec = reader.readLine()) != null)
      {
        String numberStr = "";
        int len = 0;
        int number = 0;
        int h = 0;

        switch(mode)
        {
          //1.デバック準備
          case 1:
            if(lineDec.matches(".*//[d,D][0-9]{2}.*")){
              lineArray = lineDec.toCharArray();
              len = lineArray.length;
              
              int k = 0;
              for(int i = 0; i < len; i++)
              {
                if(lineArray[i] == '/' && lineArray[i+2] == 'd' || lineArray[i+2] == 'D'){
                  numberStr += String.valueOf(lineArray[i+3]);
                  numberStr += String.valueOf(lineArray[i+4]);
                  number = Integer.parseInt(numberStr);
                  break;
                }else{
                  k++;
                }
              }

              if(number > 0 && number < 53){
                for(int i = 0; i < k; i++)
                {
                  line += lineArray[i];
                }

                if(number < 27){
                  line += "System.out.println(\"" + ALPHABET[number-1] + "\");";
                }else{
                  line += "System.out.println(\"" + alphabet[number-27] + "\");";
                }
              }else{
                line += lineDec;
              }
            }else{
              line += lineDec;
            }
          break;

          //2.デバック中断
          case 2:
            if(lineDec.matches(".*System\\.out\\.println\\(\"[A-Za-z]\"\\);.*")){
              lineArray = lineDec.toCharArray();
              len = lineArray.length;

              for(int i = 0; i < len; i++)
              {
                if(lineArray[i] == 'S'){
                  line += "//D";
                  boolean upperCase = Character.isUpperCase(String.valueOf(lineArray[i+20]).charAt(0));
                  
                  int c = 0;
                  for(; c < 26; c++)
                  {
                    if(upperCase){
                      if(lineArray[i+20] == ALPHABET[c]){
                        if(c < 10){
                          line += "0";
                        }
                        break;
                      }
                    }else{
                      if(lineArray[i+20] == alphabet[c]){
                        c += 26;
                        break;
                      }
                    }
                  }

                  line += String.valueOf(c+1);
                  break;
                }else{
                  line += lineArray[i];
                }
              }
            }else{
              line += lineDec;
            }
          break;

          //3.デバック終了
          case 3:
            if(lineDec.matches(".*System\\.out\\.println\\(\"[A-Za-z]\"\\);\\.*") || lineDec.matches(".*//[D,d][0-9]{2}.*")){
              h = 1;
            }else{
              line += lineDec;
            }
          break;
        }

        if(h == 0){
          line += "\n";
        }
      }
    }else{
      System.out.println("ファイルを見つけられませんでした");
      System.exit(0);
    }

    return line;
  }
}
