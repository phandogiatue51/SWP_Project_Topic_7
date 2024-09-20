package com.product.server.koi_control_application;

import com.product.server.koi_control_application.serviceInterface.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableAspectJAutoProxy
public class KoiControlApplication {


    public static void main(String[] args) {

        SpringApplication.run(KoiControlApplication.class, args);
    }


}
