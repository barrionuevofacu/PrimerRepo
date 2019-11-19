using IBM.Cloud.SDK.Core.Authentication.Iam;
using IBM.Cloud.SDK.Core.Http;
using IBM.Cloud.SDK.Core.Http.Exceptions;
using IBM.Watson.VisualRecognition.v3;
using IBM.Watson.VisualRecognition.v3.Model;
using Microsoft.AspNetCore.Http;
using Microsoft.IdentityModel.Tokens;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IdentityModel.Tokens.Jwt;
using System.IO;
using System.Linq;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using WebApiObjetos.Domain;
using WebApiObjetos.Models.Entities;
using WebApiObjetos.Models.Repositories;
using WebApiObjetos.Models.Repositories.Interfaces;
using WebApiObjetos.Properties;
using WebApiObjetos.Services.Interfaces;

namespace WebApiObjetos.Services
{
    //representa las columnas de la tabla, tiene sus parametros
    public class UserService : IUserService
    {
        private IUserRepository userRepo;

        public UserService(IUserRepository userRepo)
        {
            this.userRepo = userRepo;
        }

        
        public async Task<bool> SignIn(UserDTO user)
        {
            var existingUser = await userRepo.FindBy(x => x.UserName == user.UserName);

            if (existingUser.Count != 0)
                return false;

            await userRepo.Add(user.ToEntity());
            return true;
        }


        public async Task<UserDTO> Login(UserDTO user)
        {
            IamAuthenticator authenticator = new IamAuthenticator(apikey: "qcuAtCisP-Au2RPtxkVM1pU4NsYzxu_iPTw9WlYSbOaq");

            VisualRecognitionService visualRecognition = new VisualRecognitionService("2018-03-19", authenticator);
            visualRecognition.SetServiceUrl("https://gateway.watsonplatform.net/visual-recognition/api");
            DetailedResponse<ClassifiedImages> result1;
            using (FileStream fs = File.OpenRead("./Labrador_retriever_06457.jpg"))
            {
                using (MemoryStream ms = new MemoryStream())
                {
                    fs.CopyTo(ms);
                    result1 = visualRecognition.Classify(
                        //url: "https://img.pixers.pics/pho_wat(s3:700/FO/48/14/15/73/700_FO48141573_3b497c03f0d6755bb5657b67149c578d.jpg,700,507,cms:2018/10/5bd1b6b8d04b8_220x50-watermark.png,over,480,457,jpg)/vinilos-para-armario-alaskan-malamute-en-la-nieve.jpg.jpg",
                        //imagesFilename: "Akita_00227.jpg",
                        imagesFile: ms,
                        //threshold: 0.6f,
                        owners: new List<string>() { "me" }
                    );
                }
            }
            //  The result object
            var responseHeaders = result1.Headers;  //  The response headers
            var responseJson = result1.Result;    //  The raw response JSON

            var class1 = responseJson.Images.FirstOrDefault().Classifiers.FirstOrDefault().Classes.FirstOrDefault();
            var raza = class1._Class;
            var porcentaje = class1.Score;



            var statusCode = result1.StatusCode;    //  The response status code
            Console.WriteLine(result1.Response);
            Console.WriteLine("La raza es: " + raza + ". Con una precision de: " + porcentaje);

            var result = (await userRepo.FindBy(x => x.UserName == user.UserName && x.Password == user.Password)).First();

            if (result == null)
                return null;

            var token = GenerateToken(result);

            UserDTO userDto = new UserDTO()
            {
                UserName = user.UserName,
                Password = user.Password,
                Token = new JwtSecurityTokenHandler().WriteToken(token),
            };
        
            return userDto;
        }
        

        private JwtSecurityToken GenerateToken(User user)
        {
            try
            {
                var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Resources.Encription_Key));
                var cred = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
                // cuando la base tenga mas datos importantes tipo version, edad , etc, eso se mete en una claim para que no tenga que enviar esa info en cada request de los servicios
                var claim = (new[] {
                new Claim(JwtRegisteredClaimNames.Sub, user.UserName),
                new Claim(JwtRegisteredClaimNames.Iat,DateTime.UtcNow.ToString()),
                new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
                new Claim(ClaimTypes.Role,"admin"),
                new Claim("UserId", user.Id.ToString())
            });

                return new JwtSecurityToken(
                            issuer: Resources.Issuer,
                            audience: Resources.Audience,
                            claims: claim,
                            expires: DateTime.UtcNow.AddHours(Int32.Parse(Resources.Token_Duration)),//está puesto una hora para el token, se cambia en la tabla resources
                            notBefore: DateTime.UtcNow, // a partir de cuando se puede usar el token
                            signingCredentials: cred
                                            );
            }
            catch (Exception e)
            {
                throw e;
            }
        }



        //este método se usa para extraer las claims del token vencido. // no se usa pero puede resultar util para despues
        private ClaimsPrincipal GetPrincipalFromExpiredToken(string token)
        {
            var tokenValidationParameters = new TokenValidationParameters
            {
                ValidateAudience = false,
                ValidateIssuer = false,
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(Resources.Encription_Key)),
                ValidateLifetime = false //le digo que no verifique la fecha.
            };

            var tokenHandler = new JwtSecurityTokenHandler();
            SecurityToken securityToken;

            var principal = tokenHandler.ValidateToken(token, tokenValidationParameters, out securityToken);
            var jwtSecurityToken = securityToken as JwtSecurityToken;
            if (jwtSecurityToken == null || !jwtSecurityToken.Header.Alg.Equals(SecurityAlgorithms.HmacSha256, StringComparison.InvariantCultureIgnoreCase))
                throw new SecurityTokenException("Invalid token");

            return principal;
        }


    }
}
