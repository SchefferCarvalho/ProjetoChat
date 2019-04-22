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
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private boolean continuar = true;
    
    public Cliente() throws IOException {
        this.conexao = socket;
        JLabel lblMessage = new JLabel("Verificar");
        txtIP = new JTextField("127.0.0.1");
        txtPorta = new JTextField("5555");
        txtNome = new JTextField("Cliente A");
        Object[] texts = {lblMessage, txtIP, txtPorta, txtNome};
        JOptionPane.showMessageDialog(null, texts);
        pnlContent = new JPanel();
        texto = new JTextArea(24, 40);
        texto.setEditable(false);
        texto.setBackground(new Color(240, 240, 240));
        txtMsg = new JTextField(20);
        lblHistorico = new JLabel("Histórico");
        lblMsg = new JLabel("Mensagem");
        btnSend = new JButton("Enviar");
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair = new JButton("Sair");
        btnSair.setToolTipText("Sair do Chat");
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
        pnlContent.setBackground(Color.LIGHT_GRAY);
        texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        //setLocationRelativeTo(null);
        setLocation(700, 250);
        setResizable(false);
        setSize(500, 500);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void conectar() {
        try {
            socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
            ou = socket.getOutputStream();
            ouw = new OutputStreamWriter(ou);
            bfw = new BufferedWriter(ouw);
            bfw.write(txtNome.getText() + "\r\n");
            bfw.flush();
        } catch (Exception e) {
            System.exit(0);
        }
    }
    
    public void enviarMensagem(String msg) throws IOException {
        if (continuar) {
            if (msg != null && !(msg.trim().equals(""))) {
                bfw.write(msg + "\r\n");
                texto.append(txtNome.getText() + " - " + txtMsg.getText() + "\r\n");
                bfw.flush();
                txtMsg.setText("");
            }
        }
    }
    
    public void escutar() throws IOException {
        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        while (continuar) {
            if (bfr.ready()) {
                msg = bfr.readLine();
                texto.append(msg + "\r\n");
            }
        }
    }
    
    public void sair() throws IOException {
        continuar = false;
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
        System.exit(0);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals(btnSend.getActionCommand())) {
                enviarMensagem(txtMsg.getText());
            } else if (e.getActionCommand().equals(btnSair.getActionCommand())) {
                sair();
            }
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            texto.append("Servidor fora do ar! \r\n");
            continuar = false;
            //System.exit(0);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                enviarMensagem(txtMsg.getText());
            } catch (IOException ex) {
                // TODO Auto-generated catch block
                //ex.printStackTrace();
                texto.append("Servidor fora do ar! \r\n");
                continuar = false;
                //System.exit(0);
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
    
    public static void main(String[] args) throws IOException {
        Cliente app = new Cliente();
        app.conectar();
        app.escutar();
    }
}
