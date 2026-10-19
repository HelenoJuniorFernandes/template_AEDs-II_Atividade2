import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados */
    static Produto[] produtosCadastrados;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    /** Quantidade de pedidos mais recentes considerados na listagem de produtos recentes */
    static final int NUM_PEDIDOS_RECENTES = 5;

    /** Pilha de pedidos (atividade anterior): usada para listar os pedidos mais recentes */
    static Pilha<Pedido> pilhaPedidos = new Pilha<>();

    /** Fila de pedidos, armazenados por ordem de chegada (finalização) */
    static Fila<Pedido> filaPedidos = new Fila<>();
        
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
   
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar por um produto, por código");
        System.out.println("3 - Procurar por um produto, por nome");
        System.out.println("4 - Iniciar novo pedido");
        System.out.println("5 - Fechar pedido");
        System.out.println("6 - Listar produtos dos pedidos mais recentes");
        System.out.println("7 - Exibir o valor total médio dos N primeiros pedidos");
        System.out.println("8 - Exibir os primeiros pedidos com valor total acima de um determinado valor");
        System.out.println("9 - Exibir os primeiros pedidos que apresentam um determinado produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos. Arquivo-texto no formato
     * N  (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna um vetor vazio em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {
    	
    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	Produto[] produtosCadastrados;
    	
    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
    		
    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new Produto[numProdutos];
    		
    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
    			produtosCadastrados[i] = produto;
    		}
    		quantosProdutos = numProdutos;
    		
    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = null;
    	} finally {
    		arquivo.close();
    	}
    	
    	return produtosCadastrados;
    }
    
    /** Localiza um produto no vetor de produtos cadastrados, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null 
     */
    static Produto localizarProduto() {
        
    	Produto produto = null;
    	Boolean localizado = false;
    	
    	cabecalho();
    	System.out.println("Localizando um produto...");
        int idProduto = lerOpcao("Digite o código identificador do produto desejado: ", Integer.class);
        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
        	if (produtosCadastrados[i].hashCode() == idProduto) {
        		produto = produtosCadastrados[i];
        		localizado = true;
        	}
        }
        
        return produto;   
    }
    
    /** Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null
     *  @return O produto encontrado ou null, caso o produto não tenha sido localizado no vetor de produtos cadastrados.
     */
    static Produto localizarProdutoDescricao() {
        
    	Produto produto = null;
    	Boolean localizado = false;
    	String descricao;
    	
    	cabecalho();
    	System.out.println("Localizando um produto...");
    	System.out.println("Digite o nome ou a descrição do produto desejado:");
        descricao = teclado.nextLine();
        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
        	if (produtosCadastrados[i].descricao.toLowerCase().equals(descricao.toLowerCase())) {
        		produto = produtosCadastrados[i];
        		localizado = true;
    		}
        }
        
        return produto;
    }
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos() {
    	
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        for (int i = 0; i < quantosProdutos; i++) {
        	System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
        }
    }
    
    /** 
     * Inicia um novo pedido.
     * Permite ao usuário escolher e incluir produtos no pedido.
     * @return O novo pedido
     */
    public static Pedido iniciarPedido() {
    	
    	int formaPagamento = lerOpcao("Digite a forma de pagamento do pedido, sendo 1 para pagamento à vista e 2 para pagamento a prazo", Integer.class);
    	Pedido pedido = new Pedido(LocalDate.now(), formaPagamento);
    	Produto produto;
    	int numProdutos;
    	int quantidade;
    	
    	listarTodosOsProdutos();
    	System.out.println("Incluindo produtos no pedido...");
    	numProdutos = lerOpcao("Quantos produtos serão incluídos no pedido?", Integer.class);
        for (int i = 0; i < numProdutos; i++) {
        	produto = localizarProdutoDescricao();
        	if (produto == null) {
        		System.out.println("Produto não encontrado");
        		i--;
        	} else {
        		quantidade = lerOpcao("Quantos itens desse produto serão incluídos no pedido?", Integer.class);
        		pedido.incluirProduto(produto, quantidade);
        	}
        }
    	
        return pedido;
    }
    
    /**
     * Finaliza um pedido, momento no qual ele é armazenado na fila de pedidos (ordem de chegada)
     * e na pilha de pedidos (usada para listar os pedidos mais recentes).
     * @param pedido O pedido que deve ser finalizado.
     */
    public static void finalizarPedido(Pedido pedido) {
    	
    	cabecalho();
    	if (pedido == null) {
    		System.out.println("Não há pedido em andamento. Inicie um novo pedido antes de finalizá-lo.");
    	} else if (!pilhaPedidos.vazia() && pilhaPedidos.consultarTopo().equals(pedido)) {
    		System.out.println("Este pedido já foi finalizado.");
    	} else {
    		filaPedidos.enfileirar(pedido);
    		pilhaPedidos.empilhar(pedido);
    		System.out.println("Pedido finalizado com sucesso!");
    		System.out.println(pedido);
    	}
    }
    
    /**
     * Lista os produtos incluídos nos pedidos mais recentes (até NUM_PEDIDOS_RECENTES pedidos),
     * começando pelo pedido finalizado mais recentemente.
     */
    public static void listarProdutosPedidosRecentes() {
    	
    	cabecalho();
    	if (pilhaPedidos.vazia()) {
    		System.out.println("Nenhum pedido foi finalizado até o momento.");
    		return;
    	}
    	
    	int numPedidos = Math.min(NUM_PEDIDOS_RECENTES, pilhaPedidos.tamanho());
    	Pilha<Pedido> pedidosRecentes = pilhaPedidos.subPilha(numPedidos);
    	
    	System.out.println("PRODUTOS DOS " + numPedidos + " PEDIDO(S) MAIS RECENTE(S):");
    	while (!pedidosRecentes.vazia()) {
    		Pedido pedido = pedidosRecentes.desempilhar();
    		System.out.println("\nPedido " + pedido.hashCode() + ":");
    		for (ItemDePedido item : pedido.getItensDoPedido()) {
    			if (item != null) {
    				System.out.println(item.getProduto());
    			}
    		}
    	}
    }
    
    /**
     * Pergunta ao usuário quantos dos primeiros pedidos da fila devem ser considerados.
     * O valor é limitado ao número de pedidos existentes.
     * @return A quantidade de pedidos a considerar, ou -1 se a entrada for inválida.
     */
    static int lerQuantidadePedidos() {
    	
    	Integer n = lerOpcao("Quantos dos primeiros pedidos devem ser considerados?", Integer.class);
    	if (n == null || n < 0) {
    		System.out.println("Quantidade inválida.");
    		return -1;
    	}
    	return Math.min(n, filaPedidos.tamanho());
    }
    
    /** Exibe todos os pedidos de uma fila (consumindo a fila recebida). */
    static void exibirPedidos(Fila<Pedido> pedidos) {
    	
    	while (!pedidos.vazia()) {
    		System.out.println(pedidos.desenfileirar());
    	}
    }
    
    /** Exibe o valor total médio dos N primeiros pedidos da fila de pedidos. */
    public static void exibirValorMedioPrimeirosPedidos() {
    	
    	cabecalho();
    	if (filaPedidos.vazia()) {
    		System.out.println("Nenhum pedido foi finalizado até o momento.");
    		return;
    	}
    	
    	int quantidade = lerQuantidadePedidos();
    	if (quantidade <= 0) {
    		if (quantidade == 0) System.out.println("Nenhum pedido considerado.");
    		return;
    	}
    	
    	double media = filaPedidos.calcularValorMedio(Pedido::valorFinal, quantidade);
    	System.out.println(String.format("Valor total médio dos %d primeiros pedidos: R$ %.2f", quantidade, media));
    }
    
    /** Exibe, entre os N primeiros pedidos da fila, aqueles com valor total acima de um valor informado. */
    public static void exibirPrimeirosPedidosAcimaDeValor() {
    	
    	cabecalho();
    	if (filaPedidos.vazia()) {
    		System.out.println("Nenhum pedido foi finalizado até o momento.");
    		return;
    	}
    	
    	int quantidade = lerQuantidadePedidos();
    	if (quantidade < 0) {
    		return;
    	}
    	
    	System.out.println("Digite o valor mínimo (os pedidos exibidos terão valor total acima dele):");
    	double valor;
    	try {
    		valor = Double.parseDouble(teclado.nextLine().trim().replace(",", "."));
    	} catch (NumberFormatException e) {
    		System.out.println("Valor inválido.");
    		return;
    	}
    	
    	Fila<Pedido> filtrados = filaPedidos.filtrar(pedido -> pedido.valorFinal() > valor, quantidade);
    	if (filtrados.vazia()) {
    		System.out.println("Nenhum dos " + quantidade + " primeiros pedidos tem valor total acima de R$ " + String.format("%.2f", valor) + ".");
    	} else {
    		System.out.println("PEDIDOS COM VALOR TOTAL ACIMA DE R$ " + String.format("%.2f", valor) + ":");
    		exibirPedidos(filtrados);
    	}
    }
    
    /** Exibe, entre os N primeiros pedidos da fila, aqueles que contêm um determinado produto. */
    public static void exibirPrimeirosPedidosComProduto() {
    	
    	cabecalho();
    	if (filaPedidos.vazia()) {
    		System.out.println("Nenhum pedido foi finalizado até o momento.");
    		return;
    	}
    	
    	int quantidade = lerQuantidadePedidos();
    	if (quantidade < 0) {
    		return;
    	}
    	
    	Produto produto = localizarProdutoDescricao();
    	if (produto == null) {
    		System.out.println("Produto não encontrado.");
    		return;
    	}
    	
    	Fila<Pedido> filtrados = filaPedidos.filtrar(pedido -> pedido.existeNoPedido(produto) != null, quantidade);
    	if (filtrados.vazia()) {
    		System.out.println("Nenhum dos " + quantidade + " primeiros pedidos contém o produto informado.");
    	} else {
    		System.out.println("PEDIDOS QUE CONTÊM O PRODUTO \"" + produto.descricao + "\":");
    		exibirPedidos(filtrados);
    	}
    }
    
	public static void main(String[] args) {
		
		teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        
		nomeArquivoDados = "produtos.txt";
        produtosCadastrados = lerProdutos(nomeArquivoDados);
        
        Pedido pedido = null;
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> mostrarProduto(localizarProduto());
                case 3 -> mostrarProduto(localizarProdutoDescricao());
                case 4 -> pedido = iniciarPedido();
                case 5 -> finalizarPedido(pedido);
                case 6 -> listarProdutosPedidosRecentes();
                case 7 -> exibirValorMedioPrimeirosPedidos();
                case 8 -> exibirPrimeirosPedidosAcimaDeValor();
                case 9 -> exibirPrimeirosPedidosComProduto();
            }
            pausa();
        }while(opcao != 0);       

        teclado.close();    
    }
}
