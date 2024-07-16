package com.aluracursos.challengelibros.principal;

import com.aluracursos.challengelibros.model.Autor;
import com.aluracursos.challengelibros.model.DatosLibro;
import com.aluracursos.challengelibros.model.DatosResults;
import com.aluracursos.challengelibros.model.Libro;
import com.aluracursos.challengelibros.repository.AutorRepository;
import com.aluracursos.challengelibros.repository.LibroRepository;
import com.aluracursos.challengelibros.service.ConsumoAPI;
import com.aluracursos.challengelibros.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class Principal {

    // Repositorios para libros y autores
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    // Declaro el scanner para la entrada del usuario
    private Scanner teclado = new Scanner(System.in);

    // Instancio el servicio para consumir la API
    private ConsumoAPI consumoAPI = new ConsumoAPI();

    // URL base de la API a consumir
    private final String URL_BASE = "https://gutendex.com/books/?search=";

    // Instancio el conversor de datos
    private ConvierteDatos conversor = new ConvierteDatos();

    // Lista para almacenar libros y autores
    private List<Libro> libros = new ArrayList<>();
    private List<Autor> autores = new ArrayList<>();

    @Autowired
    // Constructor que recibe los repositorios como dependencias
    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    // Método que muestra el menú principal
    public void muestraMenu() {
        var opcion = -1;
        // Bucle para mostrar el menú hasta que el usuario elija salir
        while (opcion != 0) {
            var menu = """
                            
                            BIENVENIDO
                            
                    Elija la opción a través de su número para buscar el libro:
                    
                    1 - Buscar por título 
                    2 - Listar por registrados
                    3 - Listar por autores
                    4 - Listar por autores vivos en x año
                    5 - Listar por idioma del libro    
                    0 - Salir
                    """;
            System.out.println(menu);
            // Leo la opción elegida por el usuario
            opcion = teclado.nextInt();
            teclado.nextLine();

            // Llamo al método correspondiente según la opción elegida
            switch (opcion) {
                case 1 -> buscarLibroWeb();
                case 2 -> listarLibrosRegistrados();
                case 3 -> listarAutoresRegistrados();
                case 4 -> autoresVivosEnFecha();
                case 5 -> librosPorIdioma();
                case 0 -> {
                    System.out.println("Cerrando la aplicación...");
                    teclado.close();
                }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    // Método para obtener los datos del libro desde la API
    private DatosLibro getDatosLibro() {
        System.out.println("Escribe nombre del libro que desea buscar: ");
        var nombreLibro = teclado.nextLine().toLowerCase();
        var json = consumoAPI.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
        DatosResults datos = conversor.obtenerDatos(json, DatosResults.class);

        if (datos.resultadoLibro().isEmpty()) {
            return null;
        } else {
            return datos.resultadoLibro().get(0);
        }
    }

    // Método para buscar un libro en la web y guardarlo en el repositorio
    private void buscarLibroWeb() {
        DatosLibro datos = getDatosLibro();

        if (datos == null) {
            System.out.println("No se encontraron resultados.");
        } else if (libroRepository.existsByTitulo(datos.titulo())) {
            System.out.println("El título ya existe en el repositorio.");
        } else {
            Libro libro = new Libro(datos);
            libroRepository.save(libro);
            System.out.println(libro);
        }
    }

    // Método para listar todos los libros registrados
    private void listarLibrosRegistrados() {
        libros = libroRepository.findAll();
        libros.forEach(System.out::println);
    }

    // Método para listar todos los autores registrados
    private void listarAutoresRegistrados() {
        autores = autorRepository.findAll();
        Set<String> nombresUnicos = new HashSet<>();

        autores.stream()
                .filter(autor -> autor.getNombre() != null && nombresUnicos.add(autor.getNombre()))
                .forEach(System.out::println);
    }

    // Método para listar autores vivos en una fecha específica
    private void autoresVivosEnFecha() {
        System.out.println("Indica el año para consultar que autores están vivos: \n");
        var anioBuscado = teclado.nextInt();
        teclado.nextLine();

        List<Autor> autoresAnio = autorRepository.findAutoresVivosEnAnio(anioBuscado);
        if (autoresAnio.isEmpty()) {
            System.out.println("No se encontraron autores que vivieran en la fecha");
        } else {
            autoresAnio.forEach(System.out::println);
        }
    }

    // Método para listar libros por idioma
    private void librosPorIdioma() {
        // Muestra las opciones válidas de idiomas
        System.out.println("es - español");
        System.out.println("en - inglés");
        System.out.println("fr - francés");
        System.out.println("pt - portugués\n");
        String idioma;

        while (true) {
            System.out.println("Ingrese el idioma para buscar libros:");
            idioma = teclado.nextLine().toLowerCase();

            if (idioma.equals("en") || idioma.equals("es") || idioma.equals("fr") || idioma.equals("pt")) {
                List<Libro> librosIdioma = libroRepository.findByIdioma(idioma);

                if (librosIdioma.isEmpty()) {
                    System.out.println("No se encontraron libros en el idioma");
                } else {
                    librosIdioma.forEach(System.out::println);
                    System.out.println("\n Total de libros encontrados: " + librosIdioma.size());
                }
                break;
            } else {
                System.out.println("INGRESE UN DATO VÁLIDO");
            }
        }
    }
}
