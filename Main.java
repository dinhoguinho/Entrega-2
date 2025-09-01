import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    // Classe responsável por ler números de um arquivo
    private static List<Integer> lerNumeros(String arquivo) throws IOException {
        List<Integer> numeros = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (!linha.isEmpty()) {
                    numeros.add(Integer.parseInt(linha)); // Adiciona o número à lista
                }
            }
        }
        return numeros;
    }
    // Classe responsável por verificar se um número é primo em paralelo
    private static void escreverPrimos(String arquivo, List<Integer> numeros, boolean[] resultados) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo))) {
            for (int i = 0; i < numeros.size(); i++) {
                if (resultados[i]) {
                    bw.write(numeros.get(i).toString());
                    bw.newLine();
                }
            }
        }
    }

     private static void escreverTempo(String arquivo, String linha) throws IOException {
        // O argumento 'true' no FileWriter garante que o conteúdo seja adicionado ao fim do arquivo.
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivo, true))) {
            bw.write(linha);
            bw.newLine();
        }
    }

    // Método para executar o teste com nThreads e retornar o tempo de execução
    private static long executarTeste(List<Integer> numeros, int nThreads, boolean[] resultados) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        List<Thread> threads = new ArrayList<>();
        int bloco = (int) Math.ceil(numeros.size() / (double) nThreads);

        long inicio = System.currentTimeMillis();

        for (int t = 0; t < nThreads; t++) {
            int start = t * bloco;
            int end = Math.min(start + bloco, numeros.size());
            if (start >= end) break;
            Thread th = new Thread(new TarefaPrimo(numeros, resultados, start, end, lock));
            threads.add(th);
            th.start();
        }

        for (Thread th : threads) {
            th.join();
        }

        long fim = System.currentTimeMillis();
        return fim - inicio;
    }

    public static void main(String[] args) throws Exception {
        String arquivoEntrada = "Entrada01.txt";
        String arquivoSaida = "Primos.txt";
        String arquivoTempos = "TemposDeExecucao.txt";

        List<Integer> numeros = lerNumeros(arquivoEntrada);

        // Sequencial
        boolean[] resultados1 = new boolean[numeros.size()];
        for (int i = 0; i < numeros.size(); i++) {
            resultados1[i] = VerificadorPrimo.ePrimo(numeros.get(i));
        }
        escreverPrimos(arquivoSaida, numeros, resultados1);
        System.out.println("Tempo (1 thread): " + executarTeste(numeros, 1, new boolean[numeros.size()]) + " ms");

        // 5 threads
        boolean[] resultados5 = new boolean[numeros.size()];
        long t5 = executarTeste(numeros, 5, resultados5);
        System.out.println("Tempo (5 threads): " + t5 + " ms");

        // 10 threads
        boolean[] resultados10 = new boolean[numeros.size()];
        long t10 = executarTeste(numeros, 10, resultados10);
        System.out.println("Tempo (10 threads): " + t10 + " ms");
         // Salva os tempos de execução no arquivo de log
        escreverTempo(arquivoTempos, "Tempo (1 thread): " + executarTeste(numeros, 1, new boolean[numeros.size()]) + " ms");
        escreverTempo(arquivoTempos, "Tempo (5 threads): " + t5 + " ms");
        escreverTempo(arquivoTempos, "Tempo (10 threads): " + t10 + " ms");
        escreverTempo(arquivoTempos, ""); // Pula uma linha para a próxima execução
    }
}
