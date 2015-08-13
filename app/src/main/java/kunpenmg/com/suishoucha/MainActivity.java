package kunpenmg.com.suishoucha;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String BaiduFanyi ="  http://openapi.baidu.com/public/2.0/bmt/translate";
    public static String BaiduTrans = "http://openapi.baidu.com/public/2.0/translate/dict/simple";
    private String Client_id = "tYniYMIsQ1k3snBXZ7rVH8jT";
    private static MainActivity mainActivity=null;
    public MainActivity(){
        mainActivity = this;
    }
    public static MainActivity getMainActivity(){
        return mainActivity;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    public void initView() {

        findViewById(R.id.btn_trans).setOnClickListener(this);
        findViewById(R.id.btnser).setOnClickListener(this);
        findViewById(R.id.btn_fanyi).setOnClickListener(this);
        findViewById(R.id.snack_btn).setOnClickListener(this);
    }

    private Handler insHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 0:
                    String word = msg.getData().getString("word");
                    ((EditText) findViewById(R.id.et_second)).setText(word);
                    break;

                default:
                    break;
            }
        }
    };
    /**
     * 访问网络线程
     */
    private void tranThread(final String url) {
        new Thread() {
            public void run() {
                transEnTo(url);
            }
        }.start();
    }

    /**
     * 翻译
     */
    private void transEnTo(String url) {
        // path: http://fanyi.baidu.com/#en/zh/
        String putword = ((EditText) findViewById(R.id.et_first)).getText()
                .toString();
        try {

            String str = BaiduApi(putword,url);

            Message msg = new Message();
            msg.what = 0;
            Bundle bun = new Bundle();
            bun.putString("word", str);
            msg.setData(bun);
            insHandler.sendMessage(msg);

//            reader.close();
//            bufread.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public String BaiduApi(String s,String url){
        try {
            s = URLEncoder.encode(s, "utf-8");
            URL url1 = new URL(url + "?client_id=" + Client_id + "&q="
                    + s + "&from=auto&to=zh");
            URLConnection con = url1.openConnection();
            con.connect();
            InputStreamReader reader = new InputStreamReader(
                    con.getInputStream());
            BufferedReader bufread = new BufferedReader(reader);
            StringBuffer buff = new StringBuffer();
            String line;
            while ((line = bufread.readLine()) != null) {
                buff.append(line);
            }
            // 对字符进行解码
            String back = new String(buff.toString().getBytes("ISO-8859-1"),
                    "UTF-8");
            String str;
            if(url.equals(BaiduTrans)) {
                str = JsonToStringCidian(back);
            }
            else if(url.equals(BaiduFanyi)){
                str =JsonToStringFanyi(back);
            }
            else {
                str ="";
            }
            reader.close();
            bufread.close();
            return str;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 获取jsoon中翻译的内容
     *
     * @param jstring
     * @return
     */
    public String JsonToStringFanyi(String jstring) {
        try {
            JSONObject obj = new JSONObject(jstring);
            JSONArray array = obj.getJSONArray("trans_result");
            obj = array.getJSONObject(0);
            String word = obj.getString("dst");
            return word;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
    public String JsonToStringCidian(String jstring){

        try {
            JSONObject obj = new JSONObject(jstring);
            JSONObject data = obj.getJSONObject("data");
            JSONArray symbols = data.getJSONArray("symbols");


            String ph_am = "美"+"["+symbols.getJSONObject(0).getString("ph_am")+"]";
            String ph_en = "英"+"["+symbols.getJSONObject(0).getString("ph_en")+"]";
            JSONArray parts = symbols.getJSONObject(0).getJSONArray("parts");

            StringBuffer partsb = new StringBuffer();
            for(int pn = 0;pn <parts.length();pn++ ){
               partsb.append(parts.getJSONObject(pn).getString("part")+'\t');
                JSONArray means = parts.getJSONObject(pn).getJSONArray("means");
                for(int mn = 0;mn < means.length();mn++){
                    partsb.append(","+means.getString(mn));
                }
                partsb.append('\n');
            }
            String Parts = partsb.toString();
            return  ph_am+ph_en+'\n'+Parts;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";

    }



    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnser:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,BackFanyi.class);
                startService(intent);
                break;
            case R.id.btn_trans:
                tranThread(BaiduTrans);

                break;
            case  R.id.btn_fanyi:
                tranThread(BaiduFanyi);
                break;
            case R.id.snack_btn:

                Snackbar.make(getWindow().getDecorView().getRootView(),"hello world",Snackbar.LENGTH_LONG).show();
                break;



            default:
                break;
        }
    }
}
