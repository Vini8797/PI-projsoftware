package br.insper.estudoPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Esta classe PRECISA ficar no pacote raiz (br.insper.estudoPI).
// O Spring só enxerga @Service, @RestController etc. que estejam neste pacote ou abaixo dele.
@SpringBootApplication
public class EstudoPiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstudoPiApplication.class, args);
    }

}
