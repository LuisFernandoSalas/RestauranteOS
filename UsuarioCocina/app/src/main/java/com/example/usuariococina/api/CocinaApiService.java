import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import java.util.List;

public interface CocinaApiService {

    // 🚀 Llamamos al endpoint de la línea 34 de José
    @GET("pedidos/cocina")
    Call<List<PedidoCocina>> obtenerPedidosCocina(
            @Header("Authorization") String tokenBearer
    );
}