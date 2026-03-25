package com.hms.pharmacy.repository;

import com.hms.common.enums.MedicineCategory;
import com.hms.pharmacy.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, UUID>, JpaSpecificationExecutor<Medicine> {

    // SELECT * FROM medicines WHERE medicine_code = :medicineCode AND deleted = false
    Optional<Medicine> findByMedicineCode(String medicineCode);

    // SELECT EXISTS(SELECT 1 FROM medicines WHERE medicine_code = :medicineCode AND deleted = false)
    boolean existsByMedicineCode(String medicineCode);

    // SELECT * FROM medicines WHERE is_active = true AND deleted = false
    List<Medicine> findByIsActiveTrue();

    // SELECT * FROM medicines WHERE category = :category AND deleted = false
    List<Medicine> findByCategory(MedicineCategory category);

    // SELECT * FROM medicines WHERE quantity_in_stock <= :reorderLevel AND deleted = false
    List<Medicine> findByQuantityInStockLessThanEqual(Integer reorderLevel);

    // SELECT * FROM medicines WHERE LOWER(name) = LOWER(:name) AND deleted = false
    Optional<Medicine> findByNameIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock - :qty " +
           "WHERE m.id = :id AND m.quantityInStock >= :qty")
    int deductStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);

    @Modifying
    @Query("UPDATE Medicine m SET m.quantityInStock = m.quantityInStock + :qty " +
           "WHERE m.id = :id")
    void addStockAtomic(@Param("id") UUID id, @Param("qty") Integer qty);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.quantityInStock <= m.reorderLevel OR (m.reorderLevel IS NULL AND m.quantityInStock <= 10)")
    long countLowStock();
}
