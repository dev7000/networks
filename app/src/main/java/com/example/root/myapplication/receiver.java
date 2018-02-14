package com.example.root.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;


public class receiver extends AppCompatActivity {
    public Button clr,bcks,zero,one,send,strt,nack;
    public EditText txt;
    public TextView link;
    public RadioButton rbtn;
    public Thread thread,thread1,thread2;
    public int length,fl,x,y,color;
    public long st,stp;
    public CameraManager manager;
    public String CameraId, num,data="";
    public boolean bool,bool1 = false;
    public File direct;
    public String key = "111101";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver);
        clr = findViewById(R.id.button3);
        bcks = findViewById(R.id.button4);
        zero = findViewById(R.id.button);
        one = findViewById(R.id.button2);
        send = findViewById(R.id.button5);
        txt = findViewById(R.id.editText);
        nack = findViewById(R.id.button7);
        link = findViewById(R.id.textView);
        strt= findViewById(R.id.button6);
        rbtn= findViewById(R.id.radioButton);
        rbtn.setClickable(false);
        rbtn.setChecked(true);
        color=0;
        link.setClickable(true);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(),R.raw.sample);
                mp.setLooping(false);
                while(true){
                    while(bool) {
                        rbtn.post(new Runnable() {
                            @Override
                            public void run() {
                                //rbtn.setChecked(true);
                                if(color==0){
                                    color=1;
                                    rbtn.getButtonDrawable().setColorFilter(
                                            Color.GREEN, android.graphics.PorterDuff.Mode.SRC_IN);
                                }
                                else {
                                    color=0;                                
                                    rbtn.getButtonDrawable().setColorFilter(
                                            Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
                                }

                            }
                        });
                        mp.start();
                        try {
                            thread.sleep(800);
                        } catch (InterruptedException e) {
                            break;
                        }

                    }
                }
            }
        });
        thread.start();

        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length()==5) {
                    num = charSequence.toString();
                    bool1 = true;
                    synchronized (thread1){
                        thread1.notify();
                    }
                }
                if(length>0){
                    if(charSequence.length()-5 == length){
                        data=charSequence.toString();
                        warn();
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                    while(true){
                       if(bool1) {
                           int l = 5;
                           int m =1;
                           length=0;
                           while(l>0){
                               if(num.charAt(l-1)=='1'){
                                   length=length+m;
                               }
                               m=m*2;
                               l--;
                           }
                           x = (int)Math.sqrt(length);
                           if(length == x*x){
                               y=x;
                           }else if(length>x*(x+1)){
                               x=x+1;y=x+1;
                           }else{
                               y=x+1;
                           }
                           length=length+x+y+5;
                       }
                       bool1=false;
                       synchronized (thread1){
                           try {
                               thread1.wait();
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                       }
                    }
            }
        });
        thread1.start();
        nack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nack();
            }
        });
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bool=false;
                Intent receiver = new Intent(receiver.this , MainActivity.class);
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
                if (text.length() > 0)
                    txt.setText(text.substring(0, text.length() - 1));
                txt.setSelection(txt.getText().toString().length());
            }
        });
        zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txt.getText().toString();
                text = text + "0";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
            }
        });
        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = txt.getText().toString();
                text = text + "1";
                txt.setText(text);
                txt.setSelection(txt.getText().toString().length());
            }
        });

        send.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Thread t =new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String text = txt.getText().toString();
                        if (text.length()!=0) {
                            String ert =text;
                            text = parity(text);
                            String rem = encodeData(text, "111101");
                            txt.setText(text + " " + rem);
                            String num =Integer.toBinaryString(ert.length());
                            char[] chars = new char[5-num.length()];
                            Arrays.fill(chars, '0');
                            String zeros = new String(chars);
                            num=zeros+num;
                            text = "101010" +  num + text + rem;
                            int x = text.length();
                            for (int i = 0; i < x; i++) {
                                if (text.charAt(i) == '1')
                                    torchon();
                                else
                                    torchoff();
                                try {
                                    Thread.sleep(800);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            Handler hand = new Handler(getApplicationContext().getMainLooper());
                            hand.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"complete",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
                t.start();
                return true;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(data.length()!=length+5)
                    return;
                String text=data;
                text=text.substring(5);
                String rem = text.substring(text.length()-5);
                text=text.substring(0,text.length()-5);
                StringBuilder sb = new StringBuilder(text.substring(0,text.length()-1));
                String str=String.valueOf(sb.charAt(0));
                for(int i=1;i<sb.length()-x;i++){
                    if((i+1)%(x+1)!=0)
                    str=str+String.valueOf(sb.charAt(i));
                }
                String check = parity(str);
                sb =new StringBuilder(str);
                int[] ax= new int[2],ay=new int[2];
                int ex=0,ey=0;
                for(int i=x;i<check.length();i=i+x+1){
                    if(check.charAt(i)!=text.charAt(i)){
                        ay[ey]=i;
                        ey++;
                        if(ey==2){
                            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                for(int i=check.length()-x;i<check.length();i++){
                    if(check.charAt(i)!=text.charAt(i)){
                        ax[ex]=i;
                        ex++;
                        if(ex==2){
                            Toast.makeText(getApplicationContext(),"error",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if(ex==0 && ey==0){}
                else if(ex==1 && ey==1){
                    int q =x*((ay[0]+1)/(x+1) -1)+ax[0]+x-check.length();
                    int q1 =(1+x)*((ay[0]+1)/(x+1) -1)+ax[0]+x-check.length();
                    StringBuilder sb1 = new StringBuilder(text.substring(0,text.length()));
                    char z = sb.charAt(q);
                    if(z=='1'){
                        sb1.replace(q1,q1+1,"0");
                        if(eval(sb1.toString()+rem)){
                            sb.replace(q,q+1,"o");
                        }else{
                            sb.delete(0,sb.toString().length());
                        }
                    }else{
                        sb1.replace(q1,q1+1,"1");
                        if(eval(sb1.toString()+rem)){
                            sb.replace(q,q+1,"i");
                        }else{
                            sb.delete(0,sb.toString().length());
                        }
                    }

                }else if(ex!=0 && ey!=0){
                    for(int i=0;i<2;i++){
                       // char i1 = sb.charAt(ay[i] + ax[(i+1)%2]-check.length()+x);
                        //char i2 = sb.charAt(ay[(i+1)%2] + ax[i]-check.length()+x);
                        //int q =x*((ay[(i+1)%2]+1)/(x+1) -1)+ax[i]+x-check.length();
                        //int q1 =x*((ay[0]+1)/(x+1) -1)+ax[0]+x-check.length();
                        StringBuilder sb1 = new StringBuilder(text.substring(0,text.length()));
                        int i1=x*((ay[i]+1)/(x+1) -1)+ax[0]+x-check.length();
                        int i2=x*((ay[(1+i)%2]+1)/(x+1) -1)+ax[1]+x-check.length();
                        int j1=(1+x)*((ay[i]+1)/(x+1) -1)+ax[0]+x-check.length();
                        int j2=(1+x)*((ay[(1+i)%2]+1)/(x+1) -1)+ax[1]+x-check.length();
                        sb1.replace(j1,j1+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(j1)))+1)%2));
                        sb1.replace(j2,j2+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(j2)))+1)%2));
                        if(eval(sb1.toString()+rem)){
                            if(sb.charAt(i1)=='1')
                                sb.replace(i1,i1+1,"o");
                            else sb.replace(i1,i1+1,"i");
                            if(sb.charAt(i2)=='1')
                                sb.replace(i2,i2+1,"o");
                            else sb.replace(i2,i2+1,"i");
                            break;
                        }
                        if(i==1){
                            sb.delete(0,sb.toString().length());
                        }
                    }
                }else if(ex==0){
                    for(int i=0;i<x;i++){
                        int i1=(1+x)*((ay[0]+1)/(x+1) -1)+i;
                        int i2=(1+x)*((ay[1]+1)/(x+1) -1)+i;
                        StringBuilder sb1 = new StringBuilder(text.substring(0,text.length()));
                        sb1.replace(i1,i1+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(i1)))+1)%2));
                        sb1.replace(i2,i2+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(i2)))+1)%2));
                        if(eval(sb1.toString()+rem)){
                            int j1=x*((ay[0]+1)/(x+1) -1)+i;
                            int j2=x*((ay[1]+1)/(x+1) -1)+i;
                            if(sb.charAt(j1)=='1')
                                sb.replace(j1,j1+1,"o");
                            else sb.replace(j1,j1+1,"i");
                            if(sb.charAt(j2)=='1')
                                sb.replace(j2,j2+1,"o");
                            else sb.replace(j2,j2+1,"i");
                            break;
                        }
                        if(i==x-1){
                            sb.delete(0,sb.toString().length());
                        }
                    }
                }else{
                    for(int i=0;i<y;i++){
                        int i1= i*(x+1)+ ax[0]-check.length()+x;
                        int i2= i*(x+1)+ ax[1]-check.length()+x;
                        StringBuilder sb1 = new StringBuilder(text.substring(0,text.length()));
                        sb1.replace(i1,i1+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(i1)))+1)%2));
                        sb1.replace(i2,i2+1,String.valueOf((Integer.parseInt(String.valueOf(sb1.charAt(i2)))+1)%2));
                        if(eval(sb1.toString()+rem)){
                            int j1= i*x+ ax[0]-check.length()+x;
                            int j2= i*x+ ax[1]-check.length()+x;
                            if(sb.charAt(j1)=='1')
                                sb.replace(j1,j1+1,"o");
                            else sb.replace(j1,j1+1,"i");
                            if(sb.charAt(j2)=='1')
                                sb.replace(j2,j2+1,"o");
                            else sb.replace(j2,j2+1,"i");
                            break;
                        }
                        if(i==y-1){
                            sb.delete(0,sb.toString().length());
                        }
                    }
                }

                if(sb.toString()==""){
                    nack();
                    Toast.makeText(getApplicationContext(),"nack",Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        File file = new File(direct, String.valueOf(fl));
                        FileWriter fileWriter = new FileWriter(file);
                        fileWriter.write(sb.toString());
                        fileWriter.flush();
                        fileWriter.close();
                        fl++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ack();
                    Toast.makeText(getApplicationContext(),"ack",Toast.LENGTH_SHORT).show();
                }

            }

        });

        strt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread.interrupt();
                bool =true;
            }
        });

        strt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                bool=false;
                thread.interrupt();
                return false;
            }
        });

        direct = new File(Environment.getExternalStorageDirectory() + "/MyApplication");

        if(!direct.exists())
            direct.mkdir();

        //fl =direct.list().length;

        openmanager();


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

    private void ack() {
       thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                torchon();
                try {
                    thread2.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                torchoff();
            }
        });
       thread2.start();
    }

    public void warn() {
        Toast.makeText(this,"complete",Toast.LENGTH_SHORT).show();
    }

    private boolean eval(String text) {
        String rem = mod2div(text, key);
        if(Integer.parseInt(rem)==0)
            return true;
        else return false;
    }

    private void count(String txt) {
        int l = txt.length();
        length=0;
        while(l>0){
            if(txt.charAt(5-l)=='1'){
             length=length+(int)Math.pow(2,l-1);
            }
            l--;
        }
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
                if (s.charAt(i*a + j) == '1')
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
