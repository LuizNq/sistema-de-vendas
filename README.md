# Sistema de Vendas - Java Swing + PostgreSQL

Este é um projeto acadêmico desenvolvido como parte da disciplina de Banco de Dados II no curso de Engenharia de Software. A aplicação tem como objetivo simular um sistema de vendas com interface gráfica, integrando funcionalidades como registro de vendas, controle de estoque e gerenciamento de itens vendidos, utilizando Java Swing e banco de dados PostgreSQL.

## Funcionalidades

- Cadastro de vendas com associação ao funcionário
- Adição e remoção de produtos em tempo real
- Cálculo automático do valor total da venda
- Atualização automática do estoque
- Registro dos itens vendidos
- Interface gráfica intuitiva com Java Swing

## Estrutura do Banco de Dados

O sistema utiliza o seguinte esquema de tabelas:

- `tb_funcionarios`: Armazena os dados dos funcionários
- `tb_produtos`: Armazena os produtos disponíveis para venda
- `tb_vendas`: Registra cada venda realizada
- `tb_itens`: Registra os itens (produtos) pertencentes a uma venda

> Todas as operações de inserção e atualização são feitas respeitando as chaves estrangeiras e integridade relacional entre as tabelas.

## Interface do Sistema

A interface gráfica permite:

- Inserir o código do funcionário responsável pela venda
- Adicionar produtos à venda através do código e quantidade
- Exibir os produtos adicionados em uma tabela
- Finalizar a venda, persistindo os dados no banco de dados
- Limpar a venda ou remover itens específicos

## Tecnologias Utilizadas

- **Java SE** 17+
- **Swing** (GUI)
- **PostgreSQL**
- **JDBC** (para conexão com o banco de dados)

## Como Executar

### Pré-requisitos

- Java JDK 17+
- PostgreSQL instalado e em execução
- IDE de sua preferência (Eclipse, IntelliJ, NetBeans)
- Banco de dados configurado com as tabelas do sistema

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/sistema-vendas-swing.git
