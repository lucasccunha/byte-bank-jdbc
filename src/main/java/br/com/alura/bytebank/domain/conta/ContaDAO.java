package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.RegraDeNegocioException;
import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private Connection conn;

    ContaDAO(Connection connection) {
        this.conn = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf,cliente_email)" + "VALUES (?, ?, ?, ?, ?)";



        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            preparedStatement.setInt(1,conta.getNumero());
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
            preparedStatement.setString(3, dadosDaConta.dadosCliente().nome());
            preparedStatement.setString(4,dadosDaConta.dadosCliente().cpf());
            preparedStatement.setString(5,dadosDaConta.dadosCliente().email());

            preparedStatement.execute();
            preparedStatement.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Conta> listar() {
        Set<Conta> contas = new HashSet<>();
        String sql = "SELECT * FROM conta";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Integer numero = rs.getInt(1);
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome, cpf, email);
                Cliente cliente = new Cliente(dadosCliente);

                contas.add(new Conta(numero, cliente));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contas;
    }


    public Conta listarContaPorNumero(Integer numero) {
        String sql = "SELECT * FROM conta WHERE numero = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Integer numeroRecuperado = rs.getInt(1);
                    BigDecimal saldo = rs.getBigDecimal(2);
                    String nome = rs.getString(3);
                    String cpf = rs.getString(4);
                    String email = rs.getString(5);
                    DadosCadastroCliente dadosCliente = new DadosCadastroCliente(nome, cpf, email);
                    Cliente cliente = new Cliente(dadosCliente);
                    return new Conta(numeroRecuperado, cliente);
                } else {
                    throw new RegraDeNegocioException("Não existe conta cadastrada com esse número!");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
