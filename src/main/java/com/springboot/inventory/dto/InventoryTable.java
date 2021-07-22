package com.springboot.inventory.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
	@Table(name = "INVENTORY")
	public class InventoryTable implements Serializable {

		private static final long serialVersionUID = 1L;
	
		@Id
		@Column(name = "ITEM_ID")
		private String itemId;
		
		@Column(name = "INVENTORY_COUNT")
		private long inventoryCount;
		
		@Column(name = "INVENTORY_LOCATION")
		private String inventoryLocation;

		public String getItemId() {
			return itemId;
		}

		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public long getInventoryCount() {
			return inventoryCount;
		}

		public void setInventoryCount(long inventoryCount) {
			this.inventoryCount = inventoryCount;
		}

		public String getInventoryLocation() {
			return inventoryLocation;
		}

		public void setInventoryLocation(String inventoryLocation) {
			this.inventoryLocation = inventoryLocation;
		}
}
