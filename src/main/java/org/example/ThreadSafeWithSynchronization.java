package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Логирование и чтение конфига выполнено нестандартным способом.
 * Увидеть их реализацию с использованием Slf4j и spring-properties
 * вы можете в моём другом проекте на гитхабе: https://github.com/MaksimMogachev/adPlacementSystem
 */

public class ThreadSafeWithSynchronization {

  private final List<Integer> buffer = new CopyOnWriteArrayList<>();
  private final AtomicBoolean consumerIsActive = new AtomicBoolean(false);

  private final Thread PRODUCER =
      new Thread(
          () -> {
            Integer i;
            while (true) {
              if (!consumerIsActive.get()) {
                i = 0;
                while (i < new Random().nextInt(100)) {
                  Integer n = new Random().nextInt(100);
                  buffer.add(n);
                  try {
                    Thread.sleep(200);
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                  System.out.println("writing new int: " + n + ", current buffer: " + buffer);
                  i++;
                }
                System.out.println("wrote");
                consumerIsActive.set(true);
              }
            }
          });

  // Поток для чтения данных. Читает данные из списка Randoms и выводит их в консоль
  private final Thread CONSUMER =
      new Thread(
          () -> {
            while (true) {
              if (consumerIsActive.get()) {
                while (buffer.size() != 0) {
                  System.out.println("removing int: " + buffer.get(0));
                  buffer.remove(0);
                  try {
                    Thread.sleep(200);
                  } catch (InterruptedException e) {
                    e.printStackTrace();
                  }
                }
                System.out.println("read");
                consumerIsActive.set(false);
              }
            }
          });

  public void startThreadProcess() throws IOException {
    PrintStream log = new PrintStream(getConfig());
    System.setOut(log);

    PRODUCER.start();
    CONSUMER.start();
  }

  private String getConfig() throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader("./config.txt"));
    return reader.readLine().substring(11);
  }

  public static void main(String[] args) throws IOException {
    new ThreadSafeWithSynchronization().startThreadProcess();
  }
}
