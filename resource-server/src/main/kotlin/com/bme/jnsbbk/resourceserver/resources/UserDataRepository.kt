package com.bme.jnsbbk.resourceserver.resources

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDataRepository : JpaRepository<UserData, String>
