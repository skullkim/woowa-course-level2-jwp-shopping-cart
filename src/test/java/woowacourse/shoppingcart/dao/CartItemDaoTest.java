package woowacourse.shoppingcart.dao;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.jdbc.Sql;
import woowacourse.shoppingcart.domain.Product;

@JdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class CartItemDaoTest {

    private final CartItemDao cartItemDao;
    private final ProductDao productDao;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final DataSource dataSource;

    public CartItemDaoTest(final NamedParameterJdbcTemplate namedJdbcTemplate,
                           final DataSource dataSource) {
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.dataSource = dataSource;
        cartItemDao = new CartItemDao(namedJdbcTemplate, dataSource);
        productDao = new ProductDao(namedJdbcTemplate, dataSource);
    }

    @BeforeEach
    void setUp() {
        productDao.save(new Product("banana", 1_000, "woowa1.com", "banana description", 1));
        productDao.save(new Product("apple", 2_000, "woowa2.com", "apple description", 1));

        namedJdbcTemplate.update("INSERT INTO cart_item(customer_id, product_id) VALUES(:customerId, :productId)",
                Map.of("customerId", 1L, "productId", 1L));
        namedJdbcTemplate.update("INSERT INTO cart_item(customer_id, product_id) VALUES(:customerId, :productId)",
                Map.of("customerId", 1L, "productId", 2L));
    }

    @DisplayName("????????? ???????????? ?????????, ?????? ?????? ???????????? ????????????. ")
    @Test
    void addCartItem() {

        // given
        final Long customerId = 1L;
        final Long productId = 1L;

        // when
        final Long cartId = cartItemDao.addCartItem(customerId, productId, 1);

        // then
        assertThat(cartId).isEqualTo(3L);
    }

    @DisplayName("???????????? ???????????? ?????????, ?????? ??????????????? ????????? ????????? ????????? ????????? ????????????.")
    @Test
    void findProductIdsByCustomerId() {

        // given
        final Long customerId = 1L;

        // when
        final List<Long> productsIds = cartItemDao.findProductIdsByCustomerId(customerId);

        // then
        assertThat(productsIds).containsExactly(1L, 2L);
    }

    @DisplayName("Customer Id??? ?????????, ?????? ???????????? Id?????? ????????????.")
    @Test
    void findIdsByCustomerId() {

        // given
        final Long customerId = 1L;

        // when
        final List<Long> cartIds = cartItemDao.findIdsByCustomerId(customerId);

        // then
        assertThat(cartIds).containsExactly(1L, 2L);
    }

    @DisplayName("Customer Id??? ?????????, ?????? ???????????? Id?????? ????????????.")
    @Test
    void deleteCartItem() {

        // given
        final Long cartId = 1L;

        // when
        cartItemDao.deleteCartItem(cartId);

        // then
        final Long customerId = 1L;
        final List<Long> productIds = cartItemDao.findProductIdsByCustomerId(customerId);

        assertThat(productIds).containsExactly(2L);
    }
}
