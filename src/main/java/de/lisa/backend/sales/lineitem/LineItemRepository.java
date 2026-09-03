package de.lisa.backend.sales.lineitem;

import org.springframework.data.jpa.repository.JpaRepository;


interface LineItemRepository extends JpaRepository<LineItem, Long> {

}
