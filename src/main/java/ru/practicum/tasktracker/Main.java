package main.java.ru.practicum.tasktracker;

import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.enums.Status;
import main.java.ru.practicum.tasktracker.managers.Managers;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import main.java.ru.practicum.tasktracker.tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        // Простые задачи
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic1.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", epic2.getId());
        taskManager.createSubtask(subtask3);

        System.out.println("Все задачи:");
        for (Task task : taskManager.getTasks()) {
            // Используем getTask(), чтобы проверить историю
            System.out.println(taskManager.getTask(task.getId()));
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            // Используем getSubtask(), чтобы проверить историю
            System.out.println(taskManager.getSubtask(subtask.getId()));
        }

        System.out.println("\nВсе эпики:");
        for (Epic epic : taskManager.getEpics()) {
            // Используем getEpic(), чтобы проверить историю
            System.out.println(taskManager.getEpic(epic.getId()));
        }

        System.out.println("\nИстория:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }

        // Изменение статусов
        task1.setStatus(Status.DONE);
        taskManager.updateTask(task1);
        task2.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task2);

        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask3);

        System.out.println("\nВсе задачи после изменения статусов:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("\nВсе подзадачи после изменения статусов:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\nВсе эпики после изменения статусов подзадач:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }

        // Удаление задачи и эпика
        taskManager.deleteTask(task2.getId());
        taskManager.deleteEpic(epic1.getId());

        System.out.println("\nВсе задачи после удаления:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("\nВсе подзадачи после удаления:");
        for (Subtask subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("\nВсе эпики после удаления:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
        }
    }
}
