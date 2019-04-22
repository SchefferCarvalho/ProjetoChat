package ProjetoChat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import java.util.Vector;
public class Servidor extends Thread {

    //private static ArrayList<BufferedWriter> clientes;
    private static ServerSocket server;

    // Parte que controla as conexões por meio de threads.
    private static Map<String, PrintStream> MAP_CLIENTES;
    // socket deste cliente
    private Socket conexao;
    // nome deste cliente
    private String nomeCliente;
    // lista que armazena nome de CLIENTES
    private static List LISTA_DE_NOMES = new ArrayList();

    private PrintStream saida;

    // construtor que recebe o socket deste cliente
    public Servidor(Socket socket) {
        this.conexao = socket;
    }

    //testa se nomes são iguais, se for retorna true
    public boolean armazena(String newName) {
        //   System.out.println(LISTA_DE_NOMES);
        for (int i = 0; i < LISTA_DE_NOMES.size(); i++) {
            if (LISTA_DE_NOMES.get(i).equals(newName)) {
                return true;
            }
        }
        //adiciona na lista apenas se não existir
        LISTA_DE_NOMES.add(newName);
        return false;
    }

    //remove da lista os CLIENTES que já deixaram o chat
    public void remove(String oldName) {
        for (int i = 0; i < LISTA_DE_NOMES.size(); i++) {
            if (LISTA_DE_NOMES.get(i).equals(oldName)) {
                LISTA_DE_NOMES.remove(oldName);
            }
        }
    }

    public static void main(String args[]) {
        // instancia MAP_CLIENTES conectados
        MAP_CLIENTES = new HashMap();
        try {
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("5555");
            Object[] texts = {lblMessage, txtPorta};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            //clientes = new ArrayList<BufferedWriter>();
            JOptionPane.showMessageDialog(null, "Servidor ativo na porta: " + txtPorta.getText());
            System.out.println("Servidor ativo na porta - " + txtPorta.getText());
            // Loop principal.     
            while (true) {
                // aguarda algum cliente se conectar.
                // A execução do servidor fica bloqueada na chamada do método accept da
                // classe ServerSocket até que algum cliente se conecte ao servidor.
                // O próprio método desbloqueia e retorna com um objeto da classe Socket
                Socket con = server.accept();
                // cria uma nova thread para tratar essa conexão
                // voltando ao loop, esperando mais alguém se conectar.
                Thread t = new Servidor(con);
                t.start();
            }

        } catch (IOException e) {
            // caso ocorra alguma excessão de E/S, mostre qual foi.
            System.out.println(" AQUI");
            System.out.println("IOException: " + e);
        }
    }

    // execução da thread
    public void run() {
        try {
            // objetos que permitem controlar fluxo de comunicação que vem do cliente
            BufferedReader entrada = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
            saida = new PrintStream(this.conexao.getOutputStream());
            // recebe o nome do cliente
            this.nomeCliente = entrada.readLine();
            //chamada ao metodo que testa nomes iguais
            if (armazena(this.nomeCliente)) {
                saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
                //CLIENTES.add(saida);
                //fecha a conexao com este cliente
                this.conexao.close();
                return;
            } else {
                //mostra o nome do cliente conectado ao servidor
                System.out.println(this.nomeCliente + " - Conectado");
                //Quando o cliente se conectar recebe todos que estão conectados
                saida.println("Conectados: " + LISTA_DE_NOMES.toString());
            }
            //igual a null encerra a execução
            if (this.nomeCliente == null) {
                return;
            }
            //adiciona os dados de saida do cliente no objeto MAP_CLIENTES
            //A chave será o nome e valor o printstream
            MAP_CLIENTES.put(this.nomeCliente, saida);
            String[] msg = entrada.readLine().split("/");
            // Verificar se linha é null (conexão encerrada)
            // Se não for nula, mostra a troca de mensagens entre os CLIENTES
            while (msg != null) {
                System.out.println(this.nomeCliente + " - " + msg[0]);
                // reenvia a linha para todos os CLIENTES conectados
                send(saida, " - ", msg);
                // espera por uma nova linha.
                msg = entrada.readLine().split("/");
            }
            System.out.println(this.nomeCliente + " - Desconectado");
            // mensagem de saida do chat aos CLIENTES conectados
            String[] out = {"Desconectado"};
            send(saida, " - ", out);
            //remove nome da lista
            remove(this.nomeCliente);
            //exclui atributos setados ao cliente
            MAP_CLIENTES.remove(this.nomeCliente);
            //fecha a conexao com este cliente
            this.conexao.close();
        } catch (Exception e) {
            //se cliente enviar linha em branco, mostra a saida no servidor
            System.out.println(this.nomeCliente + " - Desconectado");
            // mensagem de saida do chat aos CLIENTES conectados
            for (Map.Entry cliente : MAP_CLIENTES.entrySet()) {
                PrintStream chat = (PrintStream) cliente.getValue();
                if (chat != saida) {
                    chat.println(" " + this.nomeCliente + " - Desconectado");
                }
            }
            //remove nome da lista
            remove(this.nomeCliente);
            //exclui atributos setados ao cliente
            MAP_CLIENTES.remove(this.nomeCliente);
        }
    }

    /**
     * Se o array da msg tiver tamanho igual a 1, então envia para todos Se o
     * tamanho for 2, envia apenas para o cliente escolhido
     */
    public void send(PrintStream saida, String acao, String[] msg) throws IOException {
        out:
        for (Map.Entry cliente : MAP_CLIENTES.entrySet()) {
            PrintStream chat = (PrintStream) cliente.getValue();
            if (chat != saida) {

                if (msg.length == 1) {
                    chat.println(" " + this.nomeCliente + acao + msg[0]);
                } else {
                    if (msg[1].equalsIgnoreCase((String) cliente.getKey())) {
                        chat.println(" " + this.nomeCliente + acao + msg[0]);
                        break out;
                    }
                }
            }
        }
    }
}
