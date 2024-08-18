package main.java.ru.practicum.tasktracker.managers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import main.java.ru.practicum.tasktracker.exceptions.OverlapException;
import main.java.ru.practicum.tasktracker.tasks.*;
import main.java.ru.practicum.tasktracker.enums.Status;
import java.time.LocalDateTime;
import java.util.Comparator;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())));

    // Методы для простых задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }

        tasks.clear();
        updatePrioritizedTasks();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }

        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (task.getStartTime() != null) {
            validateTask(task);
        }

        tasks.put(task.getId(), task);
        updatePrioritizedTasks();

        return task;
    }

    @Override
    public Task updateTask(Task task) {
        if (task.getStartTime() != null) {
            validateTask(task);
        }

        tasks.put(task.getId(), task);
        updatePrioritizedTasks();

        return task;
    }

    @Override
    public boolean deleteTask(int id) {
        historyManager.remove(id);
        boolean removed = tasks.remove(id) != null;
        updatePrioritizedTasks();

        return removed;
    }

    // Методы для эпиков
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }

        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updatePrioritizedTasks();

        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        updatePrioritizedTasks();

        return epic;
    }

    @Override
    public boolean deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
                historyManager.remove(subtaskId);
            }

            historyManager.remove(id);
            updatePrioritizedTasks();

            return true;
        }

        return false;
    }

    // Методы для подзадач
    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }

        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null) {
            validateTask(subtask);
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            updateEpicTimesAndDuration(epic);
        }

        updatePrioritizedTasks();
        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null) {
            validateTask(subtask);
        }

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }

        updatePrioritizedTasks();
        return subtask;
    }

    @Override
    public boolean deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(subtask.getId());
                updateEpicStatus(epic);
            }
            historyManager.remove(id);
            updatePrioritizedTasks();
            return true;
        }
        return false;
    }

    // Метод для получения истории просмотров задач
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Приватный метод для обновления статуса эпика
    private void updateEpicStatus(Epic epic) {
        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                if (subtask.getStatus() != Status.NEW) {
                    allNew = false;
                }
                if (subtask.getStatus() != Status.DONE) {
                    allDone = false;
                }
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    // Приватный метод для обновления времени и длительности эпика
    private void updateEpicTimesAndDuration(Epic epic) {
        Duration totalDuration = Duration.ZERO;
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask.getStartTime() == null) {
                continue;
            }

            totalDuration = totalDuration.plus(subtask.getDuration());
            LocalDateTime subtaskStart = subtask.getStartTime();
            LocalDateTime subtaskEnd = subtask.getEndTime();
            if (startTime == null || (subtaskStart != null && subtaskStart.isBefore(
                    startTime))) {
                startTime = subtaskStart;
            }
            if (endTime == null || (subtaskEnd != null && subtaskEnd.isAfter(
                    endTime))) {
                endTime = subtaskEnd;
            }
        }

        epic.setDuration(totalDuration);
        epic.setStartTime(startTime);
        epic.setEndTime(endTime);
    }


    // Приватный метод для обновления задач, отсортированных по времени по времени начала
    private void updatePrioritizedTasks() {
        prioritizedTasks.clear();
        tasks.values().stream().filter(task -> task.getStartTime() != null)
                .forEach(prioritizedTasks::add);
        subtasks.values().stream().filter(task -> task.getStartTime() != null)
                .forEach(prioritizedTasks::add);
    }

    // Приватный метод для проверки пересечения задачи с другой задачей по времени
    private void validateTask(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();

        boolean hasOverlap = prioritizedTasks.stream()
                .anyMatch(task -> {
                    if (task.getId() == newTask.getId()) {
                        return false;
                    }
                    LocalDateTime start = task.getStartTime();
                    LocalDateTime end = task.getEndTime();
                    return newStart.isBefore(end) && newEnd.isAfter(start);
                });

        if (hasOverlap) {
            throw new OverlapException("Задача пересекается по времени с другой задачей");
        }
    }
}
