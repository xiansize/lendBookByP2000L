package com.tc.nfc.util.email;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by tangjiarao on 16/10/18.
 */
public class SenderRunnable implements Runnable {

    private String user;
    private String password;
    private String subject;
    private String body;
    private String receiver;
    private MailSender sender;
    private String attachment;
    private Handler handler;

    public SenderRunnable(String user, String password,Handler handler) {
        this.user = user;
        this.password = password;
        this.handler =handler;
        sender = new MailSender(user, password);
        String mailhost=user.substring(user.lastIndexOf("@") + 1, user.lastIndexOf("."));
        if(!mailhost.equals("gmail")){
            mailhost="smtp."+mailhost+".com";
            Log.d("hello", mailhost);
            sender.setMailhost(mailhost);
        }
    }

    public void setMail(String subject, String body, String receiver,String attachment) {
        this.subject = subject;
        this.body = body;
        this.receiver = receiver;
        this.attachment=attachment;
    }

    public void run() {
        // TODO Auto-generated method stub
        try {
            sender.sendMail(subject, body, user, receiver,attachment);
            Message msg =new Message();
            msg.what=1;
            handler.sendMessage(msg);

        } catch (Exception e) {
            Message msg =new Message();
            msg.what=2;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }

}
