package com.example.root.myapplication;

import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public CameraManager manager;
    public String CameraId,ert="",text,rem;
    public Button clr,bcks,zero,one,send,retry;
    public EditText txt;
    public TextView link;
    public Thread t,thread2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openmanager();
        clr = findViewById(R.id.button3);
        bcks =findViewById(R.id.button4);
        zero =findViewById(R.id.button);
        one = findViewById(R.id.button2);
        send = findViewById(R.id.button5);
        txt = findViewById(R.id.editText);
        link = findViewById(R.id.textView);
        retry = findViewById(R.id.button8);
        link.setClickable(true);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent receiver = new Intent(MainActivity.this , receiver.class);
                startActivity(receiver);
            }
        });

        clr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txt.setText("");
            }
        });
        bcks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txt.getText().toString();
                if(text.length()>0)
                    txt.setText(text.substring(0, text.length() - 1));
                    txt.setSelection(txt.getText().toString().length());
            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txt.getText().toString();
                text=text+"0";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
            }
        });
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txt.getText().toString();
                text=text+"1";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
            }
        });
        zero.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String text = txt.getText().toString();
                text=text+"o";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
                return true;
            }
        });
        one.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String text = txt.getText().toString();
                text=text+"i";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
                return true;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"sending",Toast.LENGTH_SHORT).show();
                text = txt.getText().toString();
                t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(text.length()==0)return;
                        String num =Integer.toBinaryString(text.length());
                        char[] chars = new char[5-num.length()];
                        Arrays.fill(chars, '0');
                        String zeros = new String(chars);
                        num=zeros+num;
                        int count=0;
                        StringBuilder sb =new StringBuilder(text);
                        for(int i=0;i<sb.length();i++){
                            if(sb.charAt(i)=='1')
                                count=(count+1)%2;
                            else if(sb.charAt(i)=='i'){
                                sb.replace(i,i+1,"0");
                                count=(count+1)%2;
                            }else if(sb.charAt(i)=='o'){
                                sb.replace(i,i+1,"1");
                            }
                        }
                        if(count==1){
                            text=text+"1";
                        }else{
                            text=text+"0";
                        }
                        rem = encodeData(text, "111101");
                        text= "101010"+num+text+rem;
                        txt.post(new Runnable() {
                            @Override
                            public void run() {
                                txt.setText(text);
                            }
                        });
                        for (int i = 0; i < text.length(); i++) {
                            if (text.charAt(i) == '1')
                                torchon();
                            else
                                torchoff();
                            try {
                                t.sleep(800);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        send.post(new Runnable() {
                            @Override
                            public void run() {
                                send.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                t.start();
                send.setVisibility(View.INVISIBLE);
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(send.getVisibility()==View.INVISIBLE)
                t.interrupt();
                txt.setText(ert);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nack();
                        try {
                            Thread.sleep(1600);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        send.post(new Runnable() {
                            @Override
                            public void run() {
                                send.performClick();
                            }
                        });

                    }
                });
                t.start();

            }
        });


    }
    private void nack() {
        thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                while(i<3) {
                    torchon();
                    try {
                        thread2.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    torchoff();
                    try {
                        thread2.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
            }
        });
        thread2.start();
    }

    private void openmanager() {
        manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraId = manager.getCameraIdList()[0]; // back camera

        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    private void torchon(){
        try {
            manager.setTorchMode(CameraId,true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void torchoff(){
        try {
            manager.setTorchMode(CameraId,false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void calc(String txt){

    }

    private String xorr (String a, String b){
        String result = "";
        for(int k=0; k < b.length(); k++){
            if(a.charAt(k) == b.charAt(k)){
                result= result +"0";
            }
            else{
                result= result +"1";
            }
        }
        return result;
    }

    private String mod2div(String dividend, String divisor){
        int pick = divisor.length();
        String temp = dividend.substring(0, pick);
        String empty = "";
        while(pick < dividend.length()){
            if(temp.charAt(0) == '1'){
                temp = xorr(divisor, temp) + dividend.charAt(pick);
                temp = temp.substring(1);
            }
            else{
                char[] chars = new char[pick];
                Arrays.fill(chars, '0');
                String zeros = new String(chars);


                temp = xorr(zeros, temp) + dividend.charAt(pick);
                temp = temp.substring(1);
                //cout << temp << endl;
            }
            pick += 1;
        }
        if (temp.charAt(0) == '1'){
            temp = xorr(divisor, temp);
        }
        else{
            char[] chars = new char[pick];
            Arrays.fill(chars, '0');
            String zeroes = new String(chars);
            temp = xorr(zeroes, temp);
        }
        String checkword = temp;
        return checkword;
    }

    private String encodeData(String data, String key){
        int l_key = key.length();
        char[] chars = new char[l_key - 1];
        Arrays.fill(chars, '0');
        String zeross = new String(chars);
        String appended_data = data + zeross;
        //cout << appended_data << endl;
        String remainder = mod2div(appended_data, key);
        remainder = remainder.substring(1);

        String codeword = data + remainder;
        return remainder;
    }


    private String parity(String s){
        int l = s.length();
        //cout<<l<<endl;
        int m = (int)Math.sqrt(l);
        //cout<<m<<endl;
        int a,b;
        if (m*m == l)
        {
            a = m;
            b = m;
        }
        else if(l>m*(m+1))
        {
            a = m+1;
            b = m + 1;
        }else{
            a=m;
            b=m+1;
        }
        int l_key = a*b-s.length();
        char[] chars = new char[l_key];
        Arrays.fill(chars, '0');
        String zeross = new String(chars);
        s=s+zeross;
        char[] ax=new char[a];
        char[] ay=new char[b];
        //cout<<s<<endl;
        // int total = 0;
        for (int i = 0; i < b; i++)
        {
            int count = 0;
            for (int j = 0; j < a; j++)
            {
                if (s.charAt(i*a + j) == '1' || s.charAt(i*a + j) == 'i')
                {
                    count = (count + 1)%2;
                }
            }
            //cout<<count<<endl;
            if (count == 0)
            {
                ay[i]='0';
            }
            else
            {
                ay[i]='1';
            }
            //
        }
        // total = 0;
        //cout<<s<<endl;
        for (int i = 0; i < a; i++)
        {
            int count = 0;
            for (int j = 0; j < b; j++)
            {
                if (s.charAt(j*a + i) == '1')
                {
                    count = (count + 1)%2;
                }else if(s.charAt(i*a + j) == 'i'){
                    count = (count + 1)%2;
                    StringBuilder sb = new StringBuilder(s);
                    sb.replace(i*a + j,i*a + j+1,"0");
                    s=sb.toString();
                }else if(s.charAt(i*a + j) == 'o'){
                    StringBuilder sb = new StringBuilder(s);
                    sb.replace(i*a + j,i*a + j+1,"1");
                    s=sb.toString();
                }
            }
            if (count == 0)
            {
                ax[i]='0';
            }
            else
            {
                ax[i]='1';
            }
        }
        //cout<<s<<endl;
        String fin ="";
        for (int c = 0; c <l; c++)
        {
            if (c == l - 1) {
                fin = fin + s.charAt(c);
                fin = fin + ay[ay.length - 1];
            }else {
                fin = fin + s.charAt(c);
                if ((c + 1) % a == 0) {
                    int o = (c + 1) / a;
                    if (o < b)
                        fin = fin + ay[o - 1];
                }
            }
        }
        fin=fin+new String(ax);
        return fin;
    }

}
