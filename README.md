# public_api_testing

API desenvolvida em Spring Boot para extração, validação, enriquecimento e agregação de dados, com geração de relatórios em CSV.


##  Tecnologias Usadas
- Java 17+
- Spring Boot
- Spring Web
- Maven
- OpenCSV
- MySQL


---

##  Estrutura do Projeto

src/main/java/com.validation_api.validation  
├── controller  
│   ├── AggregationController  
│   ├── APIController  
│   ├── APIExtraction  
│   ├── DataEnrichmentController  
│   └── DataValidationController  
├── service  
│   ├── AggregationService  
│   ├── DataEnrichmentService  
│   └── DataValidationService  
├── model  
│   └── OperadoraANS  
└── ValidationApplication  
---

### AggregationService

AggregationService: agrega despesas por operadora/UF, calcula total, média e quantidade, e gera um CSV ordenado por maior despesa.

---

### AggregationController

Controller REST responsável por **disparar o processo de agregação de despesas** via endpoint HTTP.

Disponibiliza o endpoint:

- `GET /api/gerar-agregado`

O endpoint:
- verifica a existência do arquivo `consolidado_enriquecido.csv`
- aciona o `AggregationService` para realizar a agregação
- gera o arquivo `despesas_agregadas.csv`
- retorna informações do arquivo gerado (nome, tamanho e local)

inclui validações de erro para ausência do arquivo de entrada e falhas de processamento.

--- 
### APIExtraction

Controller responsável pela **extração de dados externos** a partir do repositório público da ANS.

Realiza:
- leitura dinâmica do diretório público de arquivos ZIP da ANS
- download automático dos arquivos CSV contidos nos ZIPs
- filtragem dos arquivos que contêm dados de **EVENTOS** ou **SINISTROS**
- extração e armazenamento dos CSVs válidos localmente
- consolidação dos dados extraídos em um único arquivo CSV
- compactação do arquivo consolidado em formato ZIP

Endpoints principais:
- `GET /downloads/ler-conteudo` – lista o conteúdo dos ZIPs disponíveis
- `GET /downloads/baixar-todos` – baixa e extrai apenas arquivos relevantes
- `GET /downloads/consolidar` – consolida os dados extraídos em um CSV final compactado

Utiliza `HttpClient` para requisições HTTP, processamento de ZIPs e leitura de arquivos CSV.

---

### DataValidationService

Service responsável pela **validação e limpeza dos dados consolidados** antes das próximas etapas do processamento.

Realiza:
- leitura do arquivo `consolidado_despesas.csv`
- validação de estrutura mínima das linhas
- verificação de campos obrigatórios (razão social e valor)
- normalização do formato numérico dos valores
- descarte de registros inválidos ou inconsistentes
- geração do arquivo `validado_despesas.csv` contendo apenas dados válidos

Inclui lógica de validação de CNPJ (dígitos verificadores), preparada para uso em validações mais rigorosas.

---

### DataEnrichmentController

Controller REST responsável por **orquestrar o processo de enriquecimento dos dados**.

Disponibiliza o endpoint:

- `GET /enrichment/executar`

O endpoint:
- utiliza o arquivo `consolidado_despesas.csv` como entrada
- aciona o `DataEnrichmentService` para enriquecer os registros
- gera um novo arquivo CSV enriquecido
- retorna o caminho do arquivo gerado como resposta

Inclui tratamento básico de exceções e resposta HTTP apropriada.

--- 
### DataEnrichmentService

Service responsável pelo **enriquecimento dos dados consolidados** com informações oficiais da ANS.

Realiza:
- download e leitura do cadastro público de operadoras da ANS
- indexação das operadoras pelo **Registro ANS**
- leitura do arquivo `consolidado_despesas.csv`
- associação dos registros financeiros com dados cadastrais da operadora
- inclusão de informações adicionais como **modalidade** e **UF**
- geração do arquivo `consolidado_enriquecido.csv` no diretório de saída

Caso uma operadora não seja encontrada no cadastro, o registro é mantido sem os dados complementares.

--- 

### OperadoraANS

Classe de modelo que representa os **dados cadastrais de uma operadora de plano de saúde**, conforme o cadastro público da ANS.

Armazena informações como:
- Registro ANS
- CNPJ
- Modalidade
- UF

É utilizada durante o processo de **enriquecimento dos dados**, servindo como estrutura para mapear e associar informações cadastrais aos registros financeiros.

---

### APIController

Controller REST responsável por realizar o **download seguro de arquivos ZIP** a partir de uma URL fornecida pelo usuário.

Disponibiliza o endpoint:

- `GET /baixar-seguro?url=<URL_DO_ZIP>`

O endpoint:
- valida se o recurso apontado pela URL é realmente um arquivo ZIP
- verifica o `Content-Type` da resposta HTTP
- evita processamento de arquivos inválidos ou malformados
- extrai o conteúdo do ZIP para o diretório local `extracao_repositorio`

Inclui tratamento de erros para URLs inválidas, respostas HTTP inesperadas e falhas durante a extração.


## Uso de Inteligência Artificial

Ferramentas de IA foram utilizadas como apoio ao desenvolvimento,
principalmente para:
- esclarecimento de dúvidas técnicas
- sugestões de refatoração
- melhoria de documentação
- correção de bugs