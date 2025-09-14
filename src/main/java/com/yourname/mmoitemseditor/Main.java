package com.yourname.mmoitemseditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlLocation = getClass().getResource("Editor.fxml");
        if (fxmlLocation == null) {
            System.err.println("错误: 找不到 FXML 文件 'Editor.fxml'");
            return;
        }

        // 获取加载器和控制器实例，以便在关闭时调用控制器的方法
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        EditorController controller = loader.getController();

        // 将Stage实例传递给控制器，以便控制器可以修改窗口标题
        controller.setStage(primaryStage);

        primaryStage.setTitle("MMOItems Editor");
        primaryStage.setScene(new Scene(root, 1200, 750));
        
        // 设置关闭请求的处理器
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (!controller.canClose()) {
                // 如果控制器说不能关闭 (因为用户取消了保存)，则消费掉这个关闭事件
                event.consume();
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}