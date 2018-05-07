 flexibleSearchService.search(/select {pk} from {user} where {passwordEncoding}='md5'/)
.result.each { user ->
   userService.setPassword(user, passwordEncoderService.decode(user.encodedPassword), 'pbkdf2')
   modelService.save user
 }
