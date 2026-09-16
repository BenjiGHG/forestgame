package com.stepflow.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int REQ = 42;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private SharedPreferences prefs;
    private StepView view;
    private long steps = 0;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("stepflow", MODE_PRIVATE);
        view = new StepView(this);
        setContentView(view);
        if (android.os.Build.VERSION.SDK_INT >= 29 && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQ);
        } else startSensor();
    }

    private void startSensor() {
        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        else { view.sensorMissing = true; view.invalidate(); }
    }

    @Override public void onRequestPermissionsResult(int r, String[] p, int[] g) {
        super.onRequestPermissionsResult(r,p,g);
        if (r == REQ && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) startSensor();
        else { view.permissionMissing = true; view.invalidate(); }
    }

    @Override public void onSensorChanged(SensorEvent e) {
        long total = (long)e.values[0];
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String savedDay = prefs.getString("day", "");
        if (!today.equals(savedDay)) {
            prefs.edit().putString("day", today).putLong("base", total).apply();
        }
        long base = prefs.getLong("base", total);
        steps = Math.max(0, total - base);
        view.invalidate();
    }
    @Override public void onAccuracyChanged(Sensor s, int a) {}
    @Override protected void onResume() { super.onResume(); if (sensorManager != null && stepSensor != null) sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI); }
    @Override protected void onPause() { super.onPause(); if (sensorManager != null) sensorManager.unregisterListener(this); }

    class StepView extends View {
        Paint p = new Paint(1); boolean permissionMissing=false, sensorMissing=false;
        StepView(Context c){ super(c); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); setBackgroundColor(0xFF0B0D10); }
        void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){ p.setTextSize(size); p.setColor(color); p.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL)); c.drawText(s,x,y,p); }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c); float w=getWidth(), h=getHeight();
            text(c,"StepFlow",28,54,27,0xFFFFFFFF,true); text(c,"DEIN TAGESFORTSCHRITT",28,82,12,0xFF8E98A8,false);
            float cx=w/2f, cy=Math.min(h*0.43f,390); float r=Math.min(w*0.30f,125); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(18); p.setStrokeCap(Paint.Cap.ROUND); p.setColor(0xFF20252D); c.drawCircle(cx,cy,r,p);
            float progress=Math.min(1f,steps/10000f); p.setColor(0xFF35D07F); c.drawArc(cx-r,cy-r,cx+r,cy+r,-90,360*progress,false,p); p.setStyle(Paint.Style.FILL);
            text(c,String.format(Locale.US,"%,d",steps),cx-p.measureText(String.format(Locale.US,"%,d",steps))/2,cy+10,42,0xFFFFFFFF,true);
            text(c,"SCHRITTE",cx-42,cy+40,13,0xFF8E98A8,true); text(c,String.format(Locale.US,"Ziel 10.000 • %d%%",Math.round(progress*100)),cx-62,cy+70,12,0xFF35D07F,false);
            float cardY=cy+r+35; float gap=14; float cw=(w-56-gap)/2f;
            card(c,28,cardY,cw,"DISTANZ",String.format(Locale.US,"%.2f km",steps*0.00072));
            card(c,28+cw+gap,cardY,cw,"AKTIVITÄT",steps>0?"In Bewegung":"Noch nicht gestartet");
            text(c,"Letzte Aktualisierung",28,cardY+132,12,0xFF687180,false);
            if(permissionMissing) text(c,"Aktivitätserlaubnis wurde nicht erteilt.",28,cardY+160,13,0xFFFFB86B,false);
            else if(sensorMissing) text(c,"Dein Gerät unterstützt keinen Schrittzähler-Sensor.",28,cardY+160,13,0xFFFFB86B,false);
        }
        void card(Canvas c,float x,float y,float ww,String title,String value){ p.setColor(0xFF15191F); c.drawRoundRect(x,y,x+ww,y+105,20,20,p); text(c,title,x+16,y+29,11,0xFF7F8997,true); text(c,value,x+16,y+67,18,0xFFFFFFFF,true); }
    }
}
