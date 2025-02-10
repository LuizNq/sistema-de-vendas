package gui;

import conexaobd.ConexaoBD;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class VendaGUI extends JFrame {
    private JTextField txtCodigoFuncionario, txtCodigoProduto, txtQuantidade;
    private JTable tabelaProdutos;
    private JButton btnAdicionarProduto, btnFinalizarVenda, btnLimpar, btnRemoverItem;
    private JLabel lblTotal;
    private double valorTotal = 0.0;
    private Connection conexao;

    public VendaGUI() {
        setTitle("Sistema de Vendas");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel superior: código do funcionário
        JPanel panelTop = new JPanel();
        panelTop.add(new JLabel("Código Funcionário:"));
        txtCodigoFuncionario = new JTextField(10);
        panelTop.add(txtCodigoFuncionario);
        add(panelTop, BorderLayout.NORTH);

        // Tabela de produtos
        String[] colunas = {"Código", "Descrição", "Quantidade", "Valor Unitário", "Total"};
        DefaultTableModel model = new DefaultTableModel(new Object[0][5], colunas);
        tabelaProdutos = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        add(scrollPane, BorderLayout.CENTER);

        // Painel inferior: entrada de produto e ações
        JPanel panelBottom = new JPanel(new GridLayout(2, 1));

        // Painel de inputs
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

        // Painel de ações (total e botões)
        JPanel panelActions = new JPanel();
        lblTotal = new JLabel("Total: R$ 0,00");
        panelActions.add(lblTotal);

        // Botão para remover item selecionado
        btnRemoverItem = new JButton("Remover Item");
        panelActions.add(btnRemoverItem);

        // Botão para limpar a venda
        btnLimpar = new JButton("Limpar Venda");
        panelActions.add(btnLimpar);

        btnFinalizarVenda = new JButton("Finalizar Venda");
        panelActions.add(btnFinalizarVenda);

        panelBottom.add(panelActions);
        add(panelBottom, BorderLayout.SOUTH);

        // Conexão com o banco de dados
        conexao = new ConexaoBD().getConexao();

        // Ações dos botões
        btnAdicionarProduto.addActionListener(e -> adicionarProduto());
        btnFinalizarVenda.addActionListener(e -> finalizarVenda());
        btnRemoverItem.addActionListener(e -> removerItem());
        btnLimpar.addActionListener(e -> limparVenda());
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

    // Remove o item selecionado na tabela e atualiza o total da venda
    private void removerItem() {
        DefaultTableModel model = (DefaultTableModel) tabelaProdutos.getModel();
        int selectedRow = tabelaProdutos.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item para remover.");
            return;
        }
        double itemTotal = Double.parseDouble(model.getValueAt(selectedRow, 4).toString());
        valorTotal -= itemTotal;
        lblTotal.setText("Total: R$ " + valorTotal);
        model.removeRow(selectedRow);
    }

    // Limpa a venda: remove os itens da tabela, zera o total e limpa os campos de entrada
    private void limparVenda() {
        DefaultTableModel model = (DefaultTableModel) tabelaProdutos.getModel();
        model.setRowCount(0);
        valorTotal = 0.0;
        lblTotal.setText("Total: R$ " + valorTotal);
        txtCodigoProduto.setText("");
        txtQuantidade.setText("");
        // Opcional: também pode limpar o campo do funcionário, se necessário
        // txtCodigoFuncionario.setText("");
    }

    private void finalizarVenda() {
        try {
            if (valorTotal == 0.0) {
                JOptionPane.showMessageDialog(this, "Nenhum produto foi adicionado.");
                return;
            }

            // Insere a venda na tabela (o código da venda será gerado automaticamente)
            String sqlVenda = "INSERT INTO tb_vendas (ven_horario, ven_valor_total, tb_funcionario_fun_codigo) VALUES (?, ?, ?)";
            PreparedStatement stmtVenda = conexao.prepareStatement(sqlVenda, new String[]{"ven_codigo"});
            stmtVenda.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmtVenda.setDouble(2, valorTotal);
            stmtVenda.setLong(3, Long.parseLong(txtCodigoFuncionario.getText()));
            stmtVenda.executeUpdate();

            // Obtém o código gerado para a venda
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
                    stmtItens.setInt(1, quantidade);
                    stmtItens.setDouble(2, valorParcial);
                    stmtItens.setLong(3, codigoProduto);
                    stmtItens.setLong(4, codigoVenda);
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
