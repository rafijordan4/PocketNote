package com.rafi.pocketnote;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(247,245,239), CARD = Color.WHITE, INK = Color.rgb(35,35,32);
    private final int MUTED = Color.rgb(130,127,117), GOLD = Color.rgb(213,169,65);
    private LinearLayout root, list;
    private ArrayList<Note> notes = new ArrayList<>();
    private boolean dark = false;

    static class Note {
        String title, body, date;
        Note(String t, String b, String d) { title=t; body=b; date=d; }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        load(); render();
    }

    private int color(int light, int night) { return dark ? night : light; }
    private GradientDrawable shape(int c, int radius) {
        GradientDrawable d = new GradientDrawable(); d.setColor(c); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String s, int size, int c, boolean bold) {
        TextView v = new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(c);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }
    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    private void pad(View v, int a, int b, int c, int d) { v.setPadding(dp(a),dp(b),dp(c),dp(d)); }
    private void render() {
        int background = color(BG, Color.rgb(23,24,23));
        int ink = color(INK, Color.rgb(245,243,235));
        int muted = color(MUTED, Color.rgb(167,164,153));
        int card = color(CARD, Color.rgb(37,39,37));
        root = column(); root.setBackgroundColor(background);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout content = column(); pad(content,22,20,22,24);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout brand = column();
        TextView eyebrow = text("YOUR PERSONAL SPACE",10,GOLD,true);
        TextView title = text("PocketNote",30,ink,true);
        brand.addView(eyebrow); brand.addView(title);
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView theme = text(dark ? "☼" : "◐",22,ink,true); theme.setGravity(Gravity.CENTER);
        theme.setBackground(shape(card,50)); header.addView(theme,new LinearLayout.LayoutParams(dp(46),dp(46)));
        theme.setOnClickListener(v -> { dark=!dark; render(); });
        content.addView(header);
        TextView subtitle = text("Simpan ide kecil, rencana, dan hal penting.",14,muted,false);
        LinearLayout.LayoutParams subp = new LinearLayout.LayoutParams(-1,-2); subp.topMargin=dp(8); subp.bottomMargin=dp(24);
        content.addView(subtitle,subp);

        LinearLayout searchWrap = new LinearLayout(this); searchWrap.setGravity(Gravity.CENTER_VERTICAL);
        searchWrap.setBackground(shape(card,15)); pad(searchWrap,15,0,15,0);
        TextView magnify = text("⌕",24,muted,false); searchWrap.addView(magnify);
        EditText search = new EditText(this); search.setSingleLine(true); search.setTextSize(14);
        search.setHint("Cari catatan..."); search.setHintTextColor(muted); search.setTextColor(ink);
        search.setBackgroundColor(Color.TRANSPARENT); search.setPadding(dp(10),dp(13),0,dp(13));
        searchWrap.addView(search,new LinearLayout.LayoutParams(0,-2,1));
        content.addView(searchWrap);
        LinearLayout section = new LinearLayout(this); section.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = text("Catatan kamu",19,ink,true);
        section.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        TextView count = text(notes.size()+" CATATAN",10,muted,true); section.addView(count);
        LinearLayout.LayoutParams secp = new LinearLayout.LayoutParams(-1,-2); secp.topMargin=dp(26); secp.bottomMargin=dp(13);
        content.addView(section,secp);

        list = column(); content.addView(list);
        if(notes.isEmpty()) {
            LinearLayout empty = column(); empty.setGravity(Gravity.CENTER); empty.setBackground(shape(card,18)); pad(empty,20,28,20,28);
            TextView symbol = text("✎",34,GOLD,true); symbol.setGravity(Gravity.CENTER);
            TextView emptyTitle = text("Belum ada catatan",17,ink,true); emptyTitle.setGravity(Gravity.CENTER);
            TextView desc = text("Tekan tombol + untuk membuat catatan pertamamu.",13,muted,false); desc.setGravity(Gravity.CENTER);
            empty.addView(symbol); empty.addView(emptyTitle); empty.addView(desc);
            list.addView(empty);
        } else {
            for(int i=0;i<notes.size();i++) addNoteCard(notes.get(i), i, card, ink, muted);
        }
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int countChars){
                String q=s.toString().toLowerCase(Locale.ROOT);
                for(int i=0;i<list.getChildCount();i++){
                    View v=list.getChildAt(i);
                    Object tag=v.getTag();
                    if(tag instanceof String) v.setVisibility(((String)tag).contains(q)?View.VISIBLE:View.GONE);
                }
            }
            public void afterTextChanged(android.text.Editable e){}
        });

        scroll.addView(content); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Button add = new Button(this); add.setText("+   Catatan baru"); add.setTextSize(15); add.setTextColor(Color.WHITE);
        add.setAllCaps(false); add.setTypeface(Typeface.DEFAULT,Typeface.BOLD); add.setBackground(shape(GOLD,16));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1,dp(54)); bp.setMargins(dp(22),dp(8),dp(22),dp(14));
        root.addView(add,bp); add.setOnClickListener(v -> editor(-1));
        setContentView(root);
    }

    private void addNoteCard(Note n, int index, int card, int ink, int muted) {
        LinearLayout item = column(); item.setBackground(shape(card,17)); pad(item,17,15,17,15);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2); p.bottomMargin=dp(11); list.addView(item,p);
        item.setTag((n.title+" "+n.body).toLowerCase(Locale.ROOT));
        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(n.title.isEmpty()?"Tanpa judul":n.title,16,ink,true);
        top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        TextView more = text("•••",18,muted,true); more.setPadding(dp(12),0,0,0); top.addView(more);
        item.addView(top);
        if(!n.body.isEmpty()) {
            TextView body = text(n.body,13,muted,false); body.setMaxLines(3); body.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2); bp.topMargin=dp(7); item.addView(body,bp);
        }
        TextView date = text(n.date,10,GOLD,true); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2); dp.topMargin=dp(12); item.addView(date,dp);
        item.setOnClickListener(v -> editor(index));
        more.setOnClickListener(v -> {
            String[] choices={"Edit","Hapus"};
            new AlertDialog.Builder(this).setItems(choices,(d,w)->{
                if(w==0) editor(index);
                else new AlertDialog.Builder(this).setMessage("Hapus catatan ini?")
                    .setNegativeButton("Batal",null).setPositiveButton("Hapus",(x,y)->{notes.remove(index);save();render();}).show();
            }).show();
        });
    }

    private void editor(int index) {
        boolean editing=index>=0;
        Note old=editing?notes.get(index):new Note("","","");
        LinearLayout box=column(); pad(box,20,8,20,0);
        EditText title=new EditText(this); title.setSingleLine(true); title.setTextSize(19); title.setHint("Judul catatan"); title.setText(old.title);
        EditText body=new EditText(this); body.setTextSize(15); body.setGravity(Gravity.TOP); body.setMinLines(7); body.setHint("Tulis sesuatu..."); body.setText(old.body);
        box.addView(title); box.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(editing?"Edit catatan":"Catatan baru")
            .setView(box).setNegativeButton("Batal",null).setPositiveButton("Simpan",null).create();
        dialog.setOnShowListener(v->dialog.getButton(-1).setOnClickListener(btn->{
            String t=title.getText().toString().trim(), b=body.getText().toString().trim();
            if(t.isEmpty()&&b.isEmpty()){title.setError("Isi judul atau catatan");return;}
            String date=new SimpleDateFormat("dd MMM yyyy • HH:mm",Locale.getDefault()).format(new Date());
            Note n=new Note(t,b,date);
            if(editing)notes.set(index,n);else notes.add(0,n);
            save();dialog.dismiss();render();
        }));
        dialog.show();
    }

    private void save() {
        JSONArray arr=new JSONArray();
        try { for(Note n:notes){JSONObject o=new JSONObject();o.put("title",n.title);o.put("body",n.body);o.put("date",n.date);arr.put(o);} }
        catch(Exception ignored){}
        getSharedPreferences("pocketnote",MODE_PRIVATE).edit().putString("notes",arr.toString()).apply();
    }
    private void load() {
        try {
            JSONArray arr=new JSONArray(getSharedPreferences("pocketnote",MODE_PRIVATE).getString("notes","[]"));
            for(int i=0;i<arr.length();i++){JSONObject o=arr.getJSONObject(i);notes.add(new Note(o.optString("title"),o.optString("body"),o.optString("date")));}
        } catch(Exception ignored){}
    }
}
