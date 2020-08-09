package Run.SpringBoot.Controllers;

import Run.SpringBoot.Service.Neo4jDataFetcher;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;

import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

@RestController
public class QueryController {

    @Autowired
    private Neo4jDataFetcher dataFetcher;

    private GraphQL graphQL;

    @PostConstruct
    public void init() throws IOException {
        URL url = Resources.getResource("graphql/schema.graphql");
        String sdl = Resources.toString(url, Charsets.UTF_8);
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }

    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(newTypeWiring("Query")

                        //Querys relativas aos algoritmos
                        .dataFetcher("detectCommunitiesWithLouvain", this.dataFetcher.louvain())
                        .dataFetcher("detectCommunitiesWithLocalClustering", this.dataFetcher.localClustering())
                        .dataFetcher("pageRank", this.dataFetcher.pageRank())
                        .dataFetcher("nodeSimilarity", this.dataFetcher.nodeSimilarity())

                        //Querys relativas as vistas e restrições
                        .dataFetcher("getListOfInvoicesNotAssociatedWithCustomers", this.dataFetcher.getListOfInvoicesNotAssociatedWithCustomers())
                        .dataFetcher("getListOfNegativeAmountsInGeneralLedger", this.dataFetcher.getListOfNegativeAmountsInGeneralLedger())
                        .dataFetcher("getListOfDaysWithoutSales", this.dataFetcher.getListOfDaysWithoutSales())
                        .dataFetcher("getListOfNetTotalAndTaxPayableByTaxCode", this.dataFetcher.getListOfNetTotalAndTaxPayableByTaxCode())
                        .dataFetcher("getListOfSalesByPeriod", this.dataFetcher.getListOfSalesByPeriod())
                )

                .build();
    }

    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

}
