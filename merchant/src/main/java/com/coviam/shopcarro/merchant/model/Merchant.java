package com.coviam.shopcarro.merchant.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * @author sreerajr
 * @package com.coviam.shopcarro.merchant.model
 * @project merchant
 */

//Check the column names

@Entity
@Table(name = "merchant")
public class Merchant {

    @Id
    @NotNull
    @Column(name = "merchant_id")
    private String merchantId;

    @NotNull
    @Column(name = "merchant_name")
    private String merchantName;

    @NotNull
    @Column(name = "no_of_products_offered")
    private Long noOfProductsOfferedToSell;

    @NotNull
    @Column(name = "no_of_products_sold")
    private Long noOfProductsSold;

    public Merchant(String merchantId, String merchantName, Long noOfProductsOfferedToSell, Long noOfProductsSold) {
        this.merchantId = merchantId;
        this.merchantName = merchantName;
        this.noOfProductsOfferedToSell = noOfProductsOfferedToSell;
        this.noOfProductsSold = noOfProductsSold;
    }

    public Merchant() {
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public Long getNoOfProductsOfferedToSell() {
        return noOfProductsOfferedToSell;
    }

    public void setNoOfProductsOfferedToSell(Long noOfProductsOfferedToSell) {
        this.noOfProductsOfferedToSell = noOfProductsOfferedToSell;
    }

    public Long getNoOfProductsSold() {
        return noOfProductsSold;
    }

    public void setNoOfProductsSold(Long noOfProductsSold) {
        this.noOfProductsSold = noOfProductsSold;
    }


    @Override
    public String toString() {
        return "Merchant{" +
                "merchantId='" + merchantId + '\'' +
                ", merchantName='" + merchantName + '\'' +
                ", noOfProductsOfferedToSell=" + noOfProductsOfferedToSell +
                ", noOfProductsSold=" + noOfProductsSold +
                '}';
    }
}
