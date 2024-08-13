package main.java.ru.practicum.tasktracker;

import java.time.Duration;
import java.time.LocalDateTime;
import main.java.ru.practicum.tasktracker.managers.Managers;
import main.java.ru.practicum.tasktracker.managers.TaskManager;
import main.java.ru.practicum.tasktracker.tasks.Epic;
import main.java.ru.practicum.tasktracker.tasks.Subtask;
import main.java.ru.practicum.tasktracker.tasks.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getFileBackedTaskManager();

        // Создаем задачи
        Task task1 = new Task(
                "Задача 1",
                "Описание задачи 1",
                Duration.ofHours(5),
                LocalDateTime.now().plusDays(5)
        );
        Task task2 = new Task(
                "Задача 2",
                "Описание задачи 2",
                Duration.ofHours(2),
                LocalDateTime.now().plusDays(6)
        );

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        // Создаем эпик и его подзадачи
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask(
                "Подзадача 1",
                "Описание подзадачи 1",
                Duration.ofHours(2),
                LocalDateTime.now().plusDays(1),
                epic1.getId()
        );
        Subtask subtask2 = new Subtask(
                "Подзадача 2",
                "Описание подзадачи 2",
                Duration.ofHours(3),
                LocalDateTime.now().plusDays(2),
                epic1.getId()
        );
        Subtask subtask3 = new Subtask(
                "Подзадача 3",
                "Описание подзадачи 3",
                Duration.ofHours(4),
                LocalDateTime.now().plusDays(3),
                epic1.getId()
        );

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        System.out.println(epic1.getEndTime());

        // Создаем еще один пустой эпик
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        taskManager.createEpic(epic2);

        // Запрашиваем задачи
        taskManager.getTask(task1.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getSubtask(subtask2.getId());
        taskManager.getEpic(epic1.getId());
        taskManager.getTask(task2.getId());
        taskManager.getSubtask(subtask3.getId());
        taskManager.getSubtask(subtask1.getId());
        taskManager.getEpic(epic2.getId());
        taskManager.getTask(task1.getId());

        // Выводим историю
        System.out.println("История:");
        printHistory(taskManager);

        // Удаляем задачу
        taskManager.deleteTask(task1.getId());

        // Выводим историю после удаления задачи
        System.out.println("\nИстория после удаления Задачи 1:");
        printHistory(taskManager);

        // Удаляем эпик
        taskManager.deleteEpic(epic1.getId());

        // Выводим историю после удаления эпика
        System.out.println("\nИстория после удаления Эпика 1:");
        printHistory(taskManager);
    }

    private static void printHistory(TaskManager taskManager) {
        taskManager.getHistory().forEach(System.out::println);
    }
}
