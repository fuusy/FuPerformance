package com.fuusy.fuperformance.appstart.dispatcher.topologicalSort

import androidx.collection.ArraySet
import com.fuusy.fuperformance.appstart.dispatcher.Task
import com.fuusy.fuperformance.appstart.dispatcher.utils.DispatcherLog
import java.util.*

object TaskSortUtil {
    private val sNewTasksHigh: MutableList<Task?> = ArrayList() // 高优先级的Task

    /**
     * 任务的有向无环图的拓扑排序
     *
     * @return
     */
    @Synchronized
    fun getSortResult(
        originTasks: List<Task?>?,
        clsLaunchTasks: List<Class<out Task>>
    ): MutableList<Task?> {
        val makeTime = System.currentTimeMillis()
        val dependSet: MutableSet<Int> = ArraySet()
        val graph = Graph(
            originTasks!!.size
        )
        for (i in originTasks.indices) {
            val task = originTasks[i]
            if (task!!.isSend || task.dependsOn() == null || task.dependsOn()!!.isEmpty()) {
                continue
            }
            for (cls in task.dependsOn()!!) {
                val indexOfDepend = getIndexOfTask(originTasks, clsLaunchTasks, cls)
                check(indexOfDepend >= 0) {
                    task.javaClass.simpleName +
                            " depends on " + cls!!.simpleName + " can not be found in task list "
                }
                dependSet.add(indexOfDepend)
                graph.addEdge(indexOfDepend, i)
            }
        }
        val indexList: List<Int?>? = graph.topologicalSort()
        val newTasksAll = getResultTasks(originTasks, dependSet, indexList)
        DispatcherLog.i("task analyse cost makeTime " + (System.currentTimeMillis() - makeTime))
        printAllTaskName(newTasksAll)
        return newTasksAll
    }

    private fun getResultTasks(
        originTasks: List<Task?>?,
        dependSet: Set<Int>, indexList: List<Int?>?
    ): MutableList<Task?> {
        val newTasksAll: MutableList<Task?> = ArrayList(
            originTasks!!.size
        )
        val newTasksDepended: MutableList<Task?> = ArrayList() // 被别人依赖的
        val newTasksWithOutDepend: MutableList<Task?> = ArrayList() // 没有依赖的
        val newTasksRunAsSoon: MutableList<Task?> = ArrayList() // 需要提升自己优先级的，先执行（这个先是相对于没有依赖的先）
        for (index in indexList!!) {
            if (dependSet.contains(index)) {
                newTasksDepended.add(originTasks[index!!])
            } else {
                val task = originTasks[index!!]
                if (task!!.needRunAsSoon()) {
                    newTasksRunAsSoon.add(task)
                } else {
                    newTasksWithOutDepend.add(task)
                }
            }
        }
        // 顺序：被别人依赖的————》需要提升自己优先级的————》需要被等待的————》没有依赖的
        sNewTasksHigh.addAll(newTasksDepended)
        sNewTasksHigh.addAll(newTasksRunAsSoon)
        newTasksAll.addAll(sNewTasksHigh)
        newTasksAll.addAll(newTasksWithOutDepend)
        return newTasksAll
    }

    private fun printAllTaskName(newTasksAll: List<Task?>) {
        return
        for (task in newTasksAll) {
            DispatcherLog.i(task!!.javaClass.simpleName)
        }
    }

    val tasksHigh: List<Task?>
        get() = sNewTasksHigh

    /**
     * 获取任务在任务列表中的index
     *
     * @param originTasks
     *
     * @return
     */
    private fun getIndexOfTask(
        originTasks: List<Task?>?,
        clsLaunchTasks: List<Class<out Task>>, cls: Class<*>?
    ): Int {
        val index = clsLaunchTasks.indexOf(cls)
        if (index >= 0) {
            return index
        }

        // 仅仅是保护性代码
        val size = originTasks!!.size
        for (i in 0 until size) {
            if (cls!!.simpleName == originTasks[i]!!.javaClass.simpleName) {
                return i
            }
        }
        return index
    }
}