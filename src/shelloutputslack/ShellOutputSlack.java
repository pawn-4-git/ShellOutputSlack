/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shelloutputslack;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import message.base.Sender2Slack;

/**
 *
 * @author Pawn
 */
public class ShellOutputSlack {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String token=null,shell=null,channel=null,output="oer";
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<args.length;i++){
            
            switch (args[i]) {
                case "-token":
                    if(i+1<args.length&&!args[i+1].equals("-shell")&&!args[i+1].equals("-channel")&&!args[i+1].equals("-output")&&!args[i+1].equals("-help")){
                        token=args[i+1];
                    }else{
                        System.out.println("token parameter is null.Please type \"-help\"");
                        System.exit(1);
                    }   break;
                case "-shell":
                    if(i+1<args.length&&!args[i+1].equals("-token")&&!args[i+1].equals("-channel")&&!args[i+1].equals("-output")&&!args[i+1].equals("-help")){
                        shell=args[i+1];
                    }else{
                        System.out.println("shell parameter is null.Please type \"-help\"");
                        System.exit(1);
                    }   break;
                case "-channel":
                    if(i+1<args.length&&!args[i+1].equals("-shell")&&!args[i+1].equals("-token")&&!args[i+1].equals("-output")&&!args[i+1].equals("-help")){
                        channel=args[i+1];
                    }else{
                        System.out.println("channel parameter is null.Please type \"-help\"");
                        System.exit(1);
                    }   break;
                case "-output":
                    if(i+1<args.length&&!args[i+1].equals("-shell")&&!args[i+1].equals("-channel")&&!args[i+1].equals("-token")&&!args[i+1].equals("-help")){
                        for(int j=0;j<args[i+1].length();j++){
                            char c=args[i+1].charAt(j);
                            if(c!='o'&&c!='e'&&c!='r'){
                                System.out.println("output parameter is invalid.Please type \"-help\"");
                                System.exit(1);
                            }
                        }
                        output=args[i+1];
                    }else{
                        System.out.println("output parameter is null.Please type \"-help\"");
                        System.exit(1);
                    }   break;
                case "-help":
                    for(int j=0;j<args.length;j++){
                       if(args[j].equals("-shell")||args[j].equals("-channel")||args[j].equals("-token")||args[j].equals("-output")){
                            System.out.println("Parameter '-shell' '-channel' '-token' '-output' cannot be useed when viewing help");
                            System.exit(1);
                       }
                      
                    }
                    /*ヘルプの表示*/
                    System.out.println("-token: (Required Item)Please write the token obtained behind  ex) -token abcdefghijklmnopqrstuvwxyz");
                    System.out.println("-shell: (Required Item)Please write the file name of the shell you want tu run behind  ex) -token /tmp/sample.sh");
                    System.out.println("-channel: Please write the channelname behind  ex) -channel general");
                    System.out.println("-output: Please write message you want behind. o...Normal execution results e...Excution result at the time of abnormality r...Excution result  ex) -output oer");
                    System.exit(0);
                default:
                    break;
            }
            
        }
        /*必須項目のチェック*/
        if(token==null){
            System.out.println("token is null.Please type \"-help\"");
            System.exit(1);
        }
        if(shell==null){
            System.out.println("shell is null.Please type \"-help\"");
            System.exit(2);
        }
        String[] out=doShell(shell);
        if(output.length()!=1){
            if(out[0]!=null&&out[0].length()!=0){
                sb=sb.append("output:\"").append(out[0]).append("\"\n\r");
            }
            if(out[1]!=null&&out[1].length()!=0){
                sb=sb.append("error=\"").append(out[1]).append("\"\n\r");
            }
            if(out[2]!=null&&out[2].length()!=0){
                sb=sb.append("response_code:\"").append(out[2]).append("\"\n\r");
            }
        }else{
            if(out[0]!=null&&out[0].length()!=0&&output.equals("o")){
                sb=sb.append("").append(out[0]).append("\n\r");
            }
            if(out[1]!=null&&out[1].length()!=0&&output.equals("e")){
                sb=sb.append("").append(out[1]).append("\n\r");
            }
            if(out[2]!=null&&out[2].length()!=0&&output.equals("r")){
                sb=sb.append("").append(out[2]).append("\n\r");
            }
        }
        Sender2Slack s2s=new Sender2Slack();
        System.out.println(sb.toString());
        if(s2s.sendMessage(token, channel,sb.toString(), Boolean.TRUE, null)){
            System.exit(0);
        }else{
            System.exit(1);
        }
    }
    
    static private String[] doShell(String path) throws UnsupportedEncodingException, IOException, Exception{
        String[] out=new String[3];
        Runtime r=Runtime.getRuntime();
        File f=new File(path);
        if(!f.exists()){
            out[1]=path+" is not found;";
            out[2]="-1";
            return out;
        }
        
        Process process=r.exec(path);
        String text="";
        
        try(InputStream is=process.getInputStream();
            InputStreamReader ir=new InputStreamReader(is,"UTF-8");
            BufferedReader br=new BufferedReader(new InputStreamReader(is));){
            StringBuilder sb=new StringBuilder();
            String line="";
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            out[0]=sb.toString();
            br.close();
            ir.close();
            is.close();
            
        }catch(Exception e){
            throw e;
        }
        try(InputStream is=process.getErrorStream();
            InputStreamReader ir=new InputStreamReader(is,"UTF-8");
            BufferedReader br=new BufferedReader(new InputStreamReader(is));){
            StringBuilder sb=new StringBuilder();
            String line="";
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            out[1]=sb.toString();
            br.close();
            ir.close();
            is.close();
            out[2]=Integer.toString(process.waitFor());
        }catch(Exception e){
            throw e;
        }
        
        return out;
    }
    
}
