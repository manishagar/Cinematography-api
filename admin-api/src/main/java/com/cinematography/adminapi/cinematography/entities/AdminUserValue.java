package com.cinematography.adminapi.cinematography.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Data 
@Builder
@Entity 
@NoArgsConstructor 
@AllArgsConstructor 
@Table(name = "users")
public class AdminUserValue {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
	@SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1, initialValue = 1)
	@Column(name = "user_id", unique = true)
	private Long userId;
	@Column(name = "username", columnDefinition = "varchar(20)")
	private String username;
	@Column(name = "password", columnDefinition = "varchar(50)")
	private String password;
	@Column(name = "name", columnDefinition = "varchar(20)")
	private String name;
	@Column(name = "surname", columnDefinition = "varchar(20)")
	private String surname;
	@Column(name = "gender", columnDefinition = "varchar(10)")
	private String gender;
	@Column(name = "status", columnDefinition = "varchar(10)")
	private String status;
	@Column(name = "expiry")
	private LocalDateTime expiry;
	@Column(name = "login_dt")
	private LocalDateTime loginDt;
	@Column(name = "creation_dt")
	private LocalDateTime creationDt;
	@Column(name = "updated_dt")
	private LocalDateTime updatedDt;
	@Column(name = "phone", columnDefinition = "varchar(20)")
	private String phone;
	@Column(name = "email", columnDefinition = "varchar(50)")
	private String email;
	@Column(name = "address_one", columnDefinition = "varchar(50)")
	private String addressOne;
	@Column(name = "address_two", columnDefinition = "varchar(50)")
	private String addressTwo;
	@Column(name = "address_three", columnDefinition = "varchar(50)")
	private String addressThree;
	@Column(name = "city", columnDefinition = "varchar(20)")
	private String city;
	@Column(name = "country", columnDefinition = "varchar(10)")
	private String country;
	@Column(name = "zip_code", columnDefinition = "varchar(20)")
	private String zipCode;
	@Column(name = "hash", columnDefinition = "varchar(50)")
	private String hash;
	@Column(name = "salt", columnDefinition = "varchar(100)")
	private String salt;
	@Column(name = "logout_dt")
	private LocalDateTime logoutDt;
	@Column(name = "created_by", columnDefinition = "varchar(50)")
	private String createdBy;
	@Column(name = "modified_by", columnDefinition = "varchar(50)")
	private String modifiedBy;
}
