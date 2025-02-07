package gui;

import conexaobd.ConexaoBD;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VendaGUI extends JFrame {
    private JTextField txtCodigoFuncionario, txtCodigoProduto, txtQuantidade;
    private JTable tabelaProdutos;
    private JButton btnAdicionarProduto, btnFinalizarVenda;
    private JLabel lblTotal;
    private double valorTotal = 0.0;
    private Connection conexao;

    public VendaGUI() {
        setTitle("Sistema de Vendas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel superior: código do funcionario
        JPanel panelTop = new JPanel();
        panelTop.add(new JLabel("Código Funcionário:"));
        txtCodigoFuncionario = new JTextField(10);
        panelTop.add(txtCodigoFuncionario);
        add(panelTop, BorderLayout.NORTH);

        // Tabela de produtos
        String[] colunas = {"Código", "Descrição", "Quantidade", "Valor Unitário", "Total"};
        DefaultTableModel model = new DefaultTableModel(new Object[0][5], colunas);
        tabelaProdutos = new JTable(model); // Aqui inicializa a tabela

        JScrollPane scrollPane = new JScrollPane(tabelaProdutos); // Adiciona a tabela dentro de um JScrollPane
        add(scrollPane, BorderLayout.CENTER); // Adiciona o JScrollPane à parte central da tela

        // Painel inferior: entrada de produto e finalização
        JPanel panelBottom = new JPanel(new GridLayout(2, 1));
        JPanel panelInputs = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInputs.add(new JLabel("Código Produto:"));
        txtCodigoProduto = new JTextField(10);
        panelInputs.add(txtCodigoProduto);
        panelInputs.add(new JLabel("Quantidade:"));
        txtQuantidade = new JTextField(5);
        panelInputs.add(txtQuantidade);
        btnAdicionarProduto = new JButton("Adicionar Produto");
        panelInputs.add(btnAdicionarProduto);
        panelBottom.add(panelInputs);

        // Painel de total e finalizar
        JPanel panelActions = new JPanel();
        lblTotal = new JLabel("Total: R$ 0,00");
        panelActions.add(lblTotal);
        btnFinalizarVenda = new JButton("Finalizar Venda");
        panelActions.add(btnFinalizarVenda);
        panelBottom.add(panelActions);

        add(panelBottom, BorderLayout.SOUTH);

        // Conexão com o banco
        conexao = new ConexaoBD().getConexao();

        btnAdicionarProduto.addActionListener(e -> adicionarProduto());
        btnFinalizarVenda.addActionListener(e -> finalizarVenda());
    }

    private void adicionarProduto() {
        try {
            long codigoFuncionario = Long.parseLong(txtCodigoFuncionario.getText());
            long codigoProduto = Long.parseLong(txtCodigoProduto.getText());
            int quantidade = Integer.parseInt(txtQuantidade.getText());

            // Verifica o produto no banco
            String sqlProduto = "SELECT * FROM tb_produtos WHERE pdt_codigo = ?";
            PreparedStatement stmtProduto = conexao.prepareStatement(sqlProduto);
            stmtProduto.setLong(1, codigoProduto);
            ResultSet rsProduto = stmtProduto.executeQuery();

            if (rsProduto.next()) {
                String descricao = rsProduto.getString("pdt_descricao");
                double valor = rsProduto.getDouble("pdt_valor");
                int estoque = rsProduto.getInt("pdt_quantidade");

                System.out.println("Código Produto: " + codigoProduto);
                System.out.println("Descrição: " + descricao);
                System.out.println("Valor: " + valor);
                System.out.println("Estoque disponível: " + estoque);

                if (estoque >= quantidade) {
                    double valorParcial = valor * quantidade;
                    valorTotal += valorParcial;
                    lblTotal.setText("Total: R$ " + valorTotal);

                    // Adiciona os dados à tabela:
                    DefaultTableModel model = (DefaultTableModel) tabelaProdutos.getModel();
                    Object[] row = new Object[] { codigoProduto, descricao, quantidade, valor, valorParcial };
                    model.addRow(row);
                } else {
                    JOptionPane.showMessageDialog(this, "Estoque insuficiente.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Produto não encontrado.");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
        }
    }

    private void finalizarVenda() {
        try {
            if (valorTotal == 0.0) {
                JOptionPane.showMessageDialog(this, "Nenhum produto foi adicionado.");
                return;
            }

            // Insere a venda na tabela sem especificar o ven_codigo, que será gerado automaticamente.
            String sqlVenda = "INSERT INTO tb_vendas (ven_horario, ven_valor_total, tb_funcionario_fun_codigo) VALUES (?, ?, ?)";
            // Especifica explicitamente que queremos retornar apenas a coluna "ven_codigo"
            PreparedStatement stmtVenda = conexao.prepareStatement(sqlVenda, new String[]{"ven_codigo"});
            stmtVenda.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmtVenda.setDouble(2, valorTotal);
            stmtVenda.setLong(3, Long.parseLong(txtCodigoFuncionario.getText()));
            stmtVenda.executeUpdate();

            // Agora, getGeneratedKeys() deve retornar somente o valor de ven_codigo
            ResultSet rs = stmtVenda.getGeneratedKeys();
            if (rs.next()) {
                long codigoVenda = rs.getLong(1);
                System.out.println("Código da Venda: " + codigoVenda);

                DefaultTableModel model = (DefaultTableModel) tabelaProdutos.getModel();
                for (int i = 0; i < model.getRowCount(); i++) {
                    long codigoProduto = Long.parseLong(model.getValueAt(i, 0).toString());
                    int quantidade = Integer.parseInt(model.getValueAt(i, 2).toString());
                    double valorParcial = Double.parseDouble(model.getValueAt(i, 4).toString());

                    // Insere o item vendido
                    String sqlItensVendidos = "INSERT INTO tb_itens (itn_codigo, itn_qtd, itn_valor_parcial, tb_produtos_pdt_codigo, tb_vendas_ven_codigo) VALUES (DEFAULT, ?, ?, ?, ?)";
                    PreparedStatement stmtItens = conexao.prepareStatement(sqlItensVendidos);
                    stmtItens.setInt(1, quantidade); // quantidade
                    stmtItens.setDouble(2, valorParcial); // valor parcial
                    stmtItens.setLong(3, codigoProduto); // código do produto
                    stmtItens.setLong(4, codigoVenda); // código da venda
                    stmtItens.executeUpdate();

                    // Atualiza o estoque
                    String sqlAtualizaEstoque = "UPDATE tb_produtos SET pdt_quantidade = pdt_quantidade - ? WHERE pdt_codigo = ?";
                    PreparedStatement stmtAtualizaEstoque = conexao.prepareStatement(sqlAtualizaEstoque);
                    stmtAtualizaEstoque.setInt(1, quantidade);
                    stmtAtualizaEstoque.setLong(2, codigoProduto);
                    stmtAtualizaEstoque.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Venda finalizada com sucesso!");
            }

        } catch (SQLException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao finalizar a venda: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VendaGUI().setVisible(true));
    }
}