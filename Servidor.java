package ProjetoChat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

public class Servidor extends Thread {

    private static ArrayList<BufferedWriter> clientes;
    private static ServerSocket server;

    // Parte que controla as conexões por meio de threads.
    private static Vector CLIENTES;
    // socket deste cliente
    private Socket conexao;
    // nome deste cliente
    private String nomeCliente;
    // lista que armazena nome de CLIENTES
    private static List LISTA_DE_NOMES = new ArrayList();

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
        // instancia o vetor de CLIENTES conectados
        CLIENTES = new Vector();
        try {
            //Cria os objetos necessário para instânciar o servidor
            JLabel lblMessage = new JLabel("Porta do Servidor:");
            JTextField txtPorta = new JTextField("5555");
            Object[] texts = {lblMessage, txtPorta};
            JOptionPane.showMessageDialog(null, texts);
            server = new ServerSocket(Integer.parseInt(txtPorta.getText()));
            clientes = new ArrayList<BufferedWriter>();
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
            System.out.println("IOException: " + e);
        }
    }

    // execução da thread
    public void run() {
        try {
            // objetos que permitem controlar fluxo de comunicação que vem do cliente
            BufferedReader entrada
                    = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));

            PrintStream saida = new PrintStream(this.conexao.getOutputStream());
            // recebe o nome do cliente
            this.nomeCliente = entrada.readLine();
            //chamada ao metodo que testa nomes iguais
            if (armazena(this.nomeCliente)) {
                saida.println("Este nome ja existe! Conecte novamente com outro Nome.");
                CLIENTES.add(saida);
                //fecha a conexao com este cliente
                this.conexao.close();
                return;
            } else {
                //mostra o nome do cliente conectado ao servidor
                System.out.println(this.nomeCliente + " - Conectado");
            }
            //igual a null encerra a execução
            if (this.nomeCliente == null) {
                return;
            }
            //adiciona os dados de saida do cliente no objeto CLIENTES
            CLIENTES.add(saida);
            //recebe a mensagem do cliente
            String msg = entrada.readLine();
            // Verificar se linha é null (conexão encerrada)
            // Se não for nula, mostra a troca de mensagens entre os CLIENTES
            while (msg != null && !(msg.trim().equals(""))) {
                System.out.println(this.nomeCliente + " - " + msg);
                // reenvia a linha para todos os CLIENTES conectados
                sendToAll(saida, " - ", msg);
                // espera por uma nova linha.
                msg = entrada.readLine();
            }
            //se cliente enviar linha em branco, mostra a saida no servidor
            System.out.println(this.nomeCliente + " - Desconectado");
            // mensagem de saida do chat aos CLIENTES conectados
            sendToAll(saida, " - ", "Desconectado");
            //remove nome da lista
            remove(this.nomeCliente);
            //exclui atributos setados ao cliente
            CLIENTES.remove(saida);
            //fecha a conexao com este cliente
            this.conexao.close();
        } catch (IOException e) {
            // Caso ocorra alguma excessão de E/S, mostre qual foi.
            //System.out.println("Falha na Conexao... .. ." + " IOException: " + e);
            //se cliente enviar linha em branco, mostra a saida no servidor
            System.out.println(this.nomeCliente + " - Desconectado");
            // mensagem de saida do chat aos CLIENTES conectados
            Enumeration en = CLIENTES.elements();
            while (en.hasMoreElements()) {
                // obtém o fluxo de saída de um dos CLIENTES
                PrintStream chat = (PrintStream) en.nextElement();
                // envia para todos, menos para o próprio usuário
                chat.println(this.nomeCliente + "- Desconectado");
            }
            //remove nome da lista
            remove(this.nomeCliente);
        }
    }

    // enviar uma mensagem para todos, menos para o próprio
    public void sendToAll(PrintStream saida, String acao, String msg) throws IOException {
        Enumeration e = CLIENTES.elements();
        while (e.hasMoreElements()) {
            // obtém o fluxo de saída de um dos CLIENTES
            PrintStream chat = (PrintStream) e.nextElement();
            // envia para todos, menos para o próprio usuário
            if (chat != saida) {
                chat.println(this.nomeCliente + acao + msg);
            }
        }
    }
}
