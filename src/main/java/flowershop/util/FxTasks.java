package flowershop.util;

import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Выполнение операций с БД в фоновом потоке, чтобы окно не «зависало»
public final class FxTasks {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "db-worker");
        thread.setDaemon(true); // не мешает закрытию приложения
        return thread;
    });

    private FxTasks() {
    }

    // Загрузка данных: результат передаётся в onSuccess уже в потоке JavaFX
    public static <T> void load(Supplier<T> work, Consumer<T> onSuccess) {
        Task<T> task = new Task<T>() {
            @Override
            protected T call() {
                return work.get();
            }
        };
        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> DialogHelper.showError(task.getException()));
        EXECUTOR.execute(task);
    }

    //Изменение данных. afterwards выполняется всегда (и после успеха, и после ошибки),
    // чтобы таблица показывала актуальное состояние БД
    public static void execute(Runnable work, Runnable afterwards) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                work.run();
                return null;
            }
        };
        task.setOnSucceeded(event -> afterwards.run());
        task.setOnFailed(event -> {
            DialogHelper.showError(task.getException());
            afterwards.run();
        });
        EXECUTOR.execute(task);
    }
}