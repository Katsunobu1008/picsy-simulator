package com.example.picsy_engine.service;

import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 直近の操作ログ（文字列）を1000件まで保持。
 * ダッシュボード右側にそのまま表示できる形式。
 */
@Service
public class ActionLogService {
    private static final int MAX=1000;
    private final Deque<String> logs=new ArrayDeque<>();

    public synchronized void log(String type, String msg){
        logs.addFirst("["+ OffsetDateTime.now() +"] ["+type+"] "+msg);
        while(logs.size()>MAX) logs.removeLast();
    }
    public synchronized List<String> list(){ return new ArrayList<>(logs); }
}
