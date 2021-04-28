package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : CrudRepository<Client, String>