import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Fila (FIFO) encadeada com célula sentinela.
 * O primeiro elemento a entrar é o primeiro a sair.
 */
public class Fila<E> {

	private Celula<E> frente;
	private Celula<E> tras;

	public Fila() {

		Celula<E> sentinela = new Celula<E>();
		frente = sentinela;
		tras = sentinela;
	}

	public boolean vazia() {
		return frente == tras;
	}

	/** Insere um item no final da fila. */
	public void enfileirar(E item) {

		Celula<E> nova = new Celula<E>(item);
		tras.setProximo(nova);
		tras = nova;
	}

	/** Remove e devolve o primeiro item da fila. */
	public E desenfileirar() {

		E desenfileirado = consultarPrimeiro();
		frente = frente.getProximo();
		return desenfileirado;
	}

	/** Devolve o primeiro item da fila, sem removê-lo. */
	public E consultarPrimeiro() {

		if (vazia()) {
			throw new NoSuchElementException("Nao há nenhum item na fila!");
		}
		return frente.getProximo().getItem();
	}

	/** Retorna a quantidade de elementos da fila. A fila não é modificada. */
	public int tamanho() {

		int quantidade = 0;
		for (Celula<E> atual = frente.getProximo(); atual != null; atual = atual.getProximo()) {
			quantidade++;
		}
		return quantidade;
	}

	/**
	 * Calcula o valor médio de um atributo numérico dos primeiros elementos da fila.
	 * A fila original não é modificada.
	 *
	 * @param extrator   função que extrai um valor numérico (Double) de cada elemento.
	 * @param quantidade quantos primeiros elementos da fila devem ser considerados.
	 * @return a média dos valores extraídos (0.0 se quantidade for 0).
	 * @throws IllegalArgumentException se quantidade for negativa ou maior que o tamanho da fila.
	 */
	public double calcularValorMedio(Function<E, Double> extrator, int quantidade) {

		verificarQuantidade(quantidade);
		if (quantidade == 0) {
			return 0.0;
		}

		double soma = 0.0;
		Celula<E> atual = frente.getProximo();
		for (int i = 0; i < quantidade; i++) {
			soma += extrator.apply(atual.getItem());
			atual = atual.getProximo();
		}
		return soma / quantidade;
	}

	/**
	 * Cria e devolve uma nova fila com os elementos, entre os primeiros
	 * "quantidade" da fila atual, que satisfazem a condição informada.
	 * A ordem relativa é mantida e a fila original não é modificada.
	 *
	 * @param condicional função que testa se um elemento deve entrar na nova fila.
	 * @param quantidade  quantos primeiros elementos da fila devem ser testados.
	 * @return nova fila com os elementos que satisfazem a condição.
	 * @throws IllegalArgumentException se quantidade for negativa ou maior que o tamanho da fila.
	 */
	public Fila<E> filtrar(Predicate<E> condicional, int quantidade) {

		verificarQuantidade(quantidade);

		Fila<E> filtrada = new Fila<E>();
		Celula<E> atual = frente.getProximo();
		for (int i = 0; i < quantidade; i++) {
			if (condicional.test(atual.getItem())) {
				filtrada.enfileirar(atual.getItem());
			}
			atual = atual.getProximo();
		}
		return filtrada;
	}

	private void verificarQuantidade(int quantidade) {

		if (quantidade < 0) {
			throw new IllegalArgumentException("A quantidade não pode ser negativa!");
		}
		if (quantidade > tamanho()) {
			throw new IllegalArgumentException("A fila não contém " + quantidade + " elementos!");
		}
	}
}
