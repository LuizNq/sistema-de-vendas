package conexaobd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class ConexaoBD {
    private String url;
    private String usuario;
    private String senha;
    private String nomeBanco;
    private Connection conexao;
    private boolean status;

    public ConexaoBD(String user, String senha, String nomeBanco) {
        this.usuario = user;
        this.senha = senha;
        this.nomeBanco = nomeBanco;

        url = "jdbc:postgresql://localhost:5433/" + nomeBanco;

        try {
            Class.forName("org.postgresql.Driver");
            conexao = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conexao Realizada Com Sucesso!!");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Nao foi possível conenectar com o Banco de dados!!");
        }

    }

    public Connection getConexao() {
        return conexao;
    }

    public ConexaoBD (){

        usuario = "postgres";
        senha = "2216";
        nomeBanco = "TrabalhoBD2.2";
        url = "jdbc:postgresql://localhost:5433/" + nomeBanco;

        try {
            Class.forName("org.postgresql.Driver");
            conexao = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conexao Realizada Com Sucesso!!");

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Nao foi possível conenectar com o Banco de dados!!");
        }

    }

    public void disconnect() {
        try {
            conexao.close();
            status = false;
            System.out.println("Fechando a conexão");
        } catch (SQLException erro) {
            System.out.println("Erro no fechamento");

        }
    }
}
