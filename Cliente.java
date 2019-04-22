package ProjetoChat;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class Cliente extends JFrame implements ActionListener, KeyListener {

    // parte que controla a recepção de mensagens do cliente
    private Socket conexao;

    private static final long serialVersionUID = 1L;
    private JTextArea texto;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JLabel lblPrivado;
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private boolean continuar = true;

    //Construtor responsavel por criar a interface gráfica
    public Cliente() throws IOException {
        this.conexao = socket;
        JLabel lblMessage1 = new JLabel("IP do Servidor");
        JLabel lblMessage2 = new JLabel("Porta");
        JLabel lblMessage3 = new JLabel("Nome do Usuário");
        txtIP = new JTextField("127.0.0.1");
        txtPorta = new JTextField("5555");
        txtNome = new JTextField("Cliente A");
        Object[] texts = {lblMessage1, txtIP, lblMessage2, txtPorta, lblMessage3, txtNome};
        JOptionPane.showMessageDialog(null, texts);
        pnlContent = new JPanel();
        texto = new JTextArea(24, 45);
        texto.setEditable(false);
        texto.setBackground(new Color(240, 240, 240));
        txtMsg = new JTextField(25);
        lblHistorico = new JLabel("Histórico");
        lblMsg = new JLabel("Mensagem");
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair = new JButton("Sair");
        btnSair.setToolTipText("Sair do Chat");
        lblPrivado = new JLabel("OBS: Para mandar uma mensagem privada digite '/' após a mensagem e o nome do Usuário");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        txtMsg.addKeyListener(this);
        JScrollPane scroll = new JScrollPane(texto);
        texto.setLineWrap(true);
        pnlContent.add(lblHistorico);
        pnlContent.add(scroll);
        pnlContent.add(lblMsg);
        pnlContent.add(txtMsg);
        pnlContent.add(btnSair);
        pnlContent.add(btnSend);
        pnlContent.add(lblPrivado);
        pnlContent.setBackground(Color.LIGHT_GRAY);
        texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocation(700, 250);
        setResizable(false);
        setSize(535, 515);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    // Metodo responsavel por realizar a conexão com o servidor
    public void conectar() {
        try {
            //Instancia do atributo conexao do tipo Socket, conecta a IP do Servidor, Porta
            socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
            //Instancia dos atributos de saida, obtem os objetos que permitem controlar o fluxo de comunicação
            ou = socket.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);
            //envia o nome digitado para o servidor
            bfw.write(txtNome.getText() + "\r\n");
            bfw.flush();
        } catch (IOException e) {
            texto.append(" Servidor fora do ar ou não encontrado! \r\n");
            continuar = false;
        }
    }

    // metodo responsavel por envia a mensagem ao servidor
    public void enviarMensagem(String msg) throws IOException {
        if (continuar) {
            if (msg != null && !(msg.trim().equals(""))) {
                // escreve a mensagem no buffer
                bfw.write(msg + "\r\n");
                // escreve a mensagem na tela
                texto.append(" " + txtNome.getText() + " - " + txtMsg.getText() + "\r\n");
                // envia a mensagem para o servidor
                bfw.flush();
                txtMsg.setText("");
            }
        }
    }

    // metodo responsavel por receber uma mensagem do servidor
    public void escutar() throws IOException {
        if (continuar) {
            InputStream in = socket.getInputStream();
            InputStreamReader inr = new InputStreamReader(in);
            BufferedReader bfr = new BufferedReader(inr);
            // Cria a variavel msg responsavel por enviar a mensagem para o servidor
            String msg = "";
            while (continuar) {
                if (bfr.ready()) {
                    // lê a mensagem
                    msg = bfr.readLine();
                    // escreve a mensagem na tela
                    texto.append(" " + msg + "\r\n");
                }
            }
        }
    }

    // finaliza todas as ações do cliente para fecha-lo 
    public void sair() throws IOException {
        if (continuar) {
            continuar = false;
            bfw.close();
            ouw.close();
            ou.close();
            socket.close();
        }
        System.exit(0);
    }

    // metodo para ler os comandos da interface grafica
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(btnSend.getActionCommand())) {
                enviarMensagem(txtMsg.getText());
            } else if (e.getActionCommand().equals(btnSair.getActionCommand())) {
                sair();
            }
        } catch (IOException ex) {
            texto.append(" Servidor fora do ar ou não encontrado! \r\n");
            continuar = false;
        }
    }

    // metodo para ler os comandos da interface grafica
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                enviarMensagem(txtMsg.getText());
            } catch (IOException ex) {
                texto.append(" Servidor fora do ar ou não encontrado! \r\n");
                continuar = false;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub               
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub               
    }

    // metodo main, responsavel por chamar os demais metodos
    public static void main(String[] args) throws IOException {
        Cliente app = new Cliente();
        app.conectar();
        app.escutar();
    }
}
