package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;

public class ContaService {

    private ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }


    public Set<Conta> listarContasAbertas() {
        Connection conn = connection.recuperaConexao();
        return new ContaDAO(conn).listar();
    }

    public Conta listarContaPorNumero(Integer numeroDaConta) {
        Connection conn = connection.recuperaConexao();
        return new ContaDAO(conn).listarContaPorNumero(numeroDaConta);
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = listarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection conn = connection.recuperaConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = listarContaPorNumero(numeroDaConta);
        if (conta == null) {
            throw new RegraDeNegocioException("Conta não existe!");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }
        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }
        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }
        BigDecimal novoValor = conta.getSaldo().subtract(valor);
        alterar(conta.getNumero(), novoValor);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = listarContaPorNumero(numeroDaConta);
        if (conta == null) {
            throw new RegraDeNegocioException("Conta não existe!");
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }
        if (!conta.getEstaAtiva()) {
            throw new RegraDeNegocioException("Conta não está ativa");
        }

        BigDecimal novoValor = conta.getSaldo().add(valor);
        alterar(conta.getNumero(), novoValor);
    }

    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino, BigDecimal valor) {
        var contaOrigem = listarContaPorNumero(numeroDaContaOrigem);
        var contaDestino = listarContaPorNumero(numeroDaContaDestino);

        if (contaOrigem == null || contaDestino == null) {
            throw new RegraDeNegocioException("Conta de origem ou destino não existe!");
        }
        this.realizarSaque(numeroDaContaOrigem, valor);
        this.realizarDeposito(numeroDaContaDestino, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = listarContaPorNumero(numeroDaConta);
        if (conta == null) {
            throw new RegraDeNegocioException("Conta não existe!");
        }
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperaConexao();

        new ContaDAO(conn).deletar(numeroDaConta);
    }
    public void encerrarLogico(Integer numeroDaConta) {
        var conta = listarContaPorNumero(numeroDaConta);
        if (conta == null) {
            throw new RegraDeNegocioException("Conta não existe!");
        }
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = connection.recuperaConexao();

        new ContaDAO(conn).alterarLogico(numeroDaConta);
    }

    private void alterar(Integer numero, BigDecimal novoValor) {
        Connection conn = connection.recuperaConexao();
        new ContaDAO(conn).alterar(numero, novoValor);
    }
}
