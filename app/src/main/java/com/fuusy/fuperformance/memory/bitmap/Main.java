package com.fuusy.fuperformance.memory.bitmap;


import com.squareup.haha.perflib.ClassObj;
import com.squareup.haha.perflib.Heap;

import com.squareup.haha.perflib.HprofParser;
import com.squareup.haha.perflib.Instance;
import com.squareup.haha.perflib.io.HprofBuffer;
import com.squareup.haha.perflib.io.MemoryMappedFileBuffer;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            File heapDumpFile = new File(args[0]);
            //打开hprof文件
            HprofBuffer buffer = new MemoryMappedFileBuffer(heapDumpFile);
            HprofParser parser = new HprofParser(buffer);
            //解析获得快照
            com.squareup.haha.perflib.Snapshot snapshot = parser.parse();
            snapshot.computeDominators();

            //获得Bitmap Class
            Collection<ClassObj> bitmapClasses = snapshot.findClasses("android.graphics.Bitmap");
            //获取堆数据,这里包括项目app、系统、default heap的信息，需要进行过滤
            Collection<Heap> heaps = snapshot.getHeaps();
//            Tools.print("bitmapClasses size = " + bitmapClasses.size());
//            Tools.print("all heaps size in snapshot = " + heaps.size());
            
            //这里有一个坑,其实snapshot也是从每个heap上获取他的ClassObj列表的,但是可能出现这个heap上的
            //ClassObj对象出现在了另一个heap中的情况,因此我们不能直接获取heap的ClassObj列表,
            //需要直接从snapshot总获取ClassObj列表.
            long startTime = System.currentTimeMillis();
            Tools.print("---------------------- 开始 ----------------------- ");
            for (Heap heap : heaps) {
                // 只需要分析app和default heap即可
                if (!heap.getName().equals("app") && !heap.getName().equals("default")) {
                    continue;
                }
                Tools.print("HeapName:" + heap.getName());

                Map<Integer, List<AnalyzerResult>> map = new HashMap<>();
                
                for (ClassObj clazz : bitmapClasses) {
                    //从heap中获得所有的Bitmap实例
                    List<Instance> instances = clazz.getHeapInstances(heap.getId());

                    for (int i = 0; i < instances.size(); i++) {
                        //从GcRoot开始遍历搜索，Integer.MAX_VALUE代表无法被搜索到，说明对象没被引用可以被回收
                        if (instances.get(i).getDistanceToGcRoot() == Integer.MAX_VALUE) {
                            continue;
                        }
                        List<AnalyzerResult> analyzerResults;
                        int curHashCode = Tools.getHashCodeByInstance(instances.get(i));
                        AnalyzerResult result = Tools.getAnalyzerResult(instances.get(i));
                        result.setInstance(instances.get(i));
                        if (map.get(curHashCode) == null){
                            analyzerResults = new ArrayList<>();
                        }else {
                            analyzerResults = map.get(curHashCode);
                        }
                        analyzerResults.add(result);
                        map.put(curHashCode, analyzerResults);
                    }
                }

                if (map.isEmpty()){
                    Tools.print("当前head暂无bitmap对象");
                }

                for (Map.Entry<Integer, List<AnalyzerResult>> entry : map.entrySet()){
                    List<AnalyzerResult> analyzerResults = entry.getValue();
                    //去除size小于2的，剩余的为重复图片。
                    if (analyzerResults.size() < 2){
                        continue;
                    }
                    Tools.print("============================================================");
                    Tools.print("duplcateCount:" + analyzerResults.size());
                    Tools.print("stacks:[");
                    for (AnalyzerResult result : analyzerResults){
                        Tools.print("   [");
                        Tools.getStackInfo(result.getInstance());
                        Tools.print("   ]");
                    }
                    Tools.print("]");
                    Tools.print(analyzerResults.get(0).toString());
                    Tools.print("============================================================");
                }
              
            }
            long endTime = System.currentTimeMillis();
            Tools.print("---------------------- 结束 ----------------------- ");
            Tools.print("处理耗时时长:" + (endTime - startTime) + "ms");
        } else {
            System.out.println("请传入文件地址");
        }
       
    }

}
